package tbank.ab.wiring

import cats.~>
import cats.effect.{Async, MonadCancelThrow, Resource}
import cats.syntax.all.*
import dev.profunktor.redis4cats.{Redis, RedisCommands}
import dev.profunktor.redis4cats.codecs.Codecs
import dev.profunktor.redis4cats.data.*
import dev.profunktor.redis4cats.effect.Log.NoOp.instance
import fs2.aws.s3.S3
import io.laserdisc.pure.s3.tagless.Interpreter
import io.lettuce.core.{ClientOptions, TimeoutOptions}
import org.http4s.client.Client
import org.http4s.client.middleware.Logger
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.otel4s.middleware.trace.AttributeProvider
import org.http4s.otel4s.middleware.trace.client.{ClientMiddleware, ClientSpanDataProvider}
import org.http4s.otel4s.middleware.trace.client.UriRedactor.OnlyRedactUserInfo
import org.typelevel.ci.CIStringSyntax
import org.typelevel.otel4s.trace.{Tracer, TracerProvider}
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient
import tbank.ab.config.{AppConfig, RedisConfig, S3Config}
import tbank.ab.domain.RequestContext
import tbank.ab.domain.auth.{AccessToken, TokenInfo}
import tofu.WithProvide
import tofu.logging.Logging

import java.net.URI
import java.time.Duration

case class Clients[F[_]]()(using
  val httpClient: Client[F]
)

object Clients {

  def make[I[_]: Async, F[_]: Async: TracerProvider](using
    config: AppConfig,
    logMake: Logging.Make[F],
    withProvide: WithProvide[F, I, RequestContext]
  ): Resource[I, Clients[F]] = {
    import config.given

    def s3StreamResource(using s3Config: S3Config): Resource[I, S3[I]] = {
      val credentials = AwsBasicCredentials.create(s3Config.accessKey, s3Config.secretKey)

      Interpreter[I].S3AsyncClientOpResource(
        S3AsyncClient
          .builder()
          .forcePathStyle(true)
          .credentialsProvider(StaticCredentialsProvider.create(credentials))
          .endpointOverride(URI.create(s3Config.uri))
          .region(Region.US_EAST_1)
      ).map(S3.create[I])
    }

    def redisResource(using redisConfig: RedisConfig): Resource[I, RedisCommands[I, AccessToken, TokenInfo]] = {
      val clientOptions = ClientOptions.builder()
        .autoReconnect(false)
        .pingBeforeActivateConnection(true)
        .timeoutOptions(
          TimeoutOptions.builder()
            .fixedTimeout(Duration.ofSeconds(10))
            .build()
        )
        .build()

      val redisCodec: RedisCodec[AccessToken, TokenInfo] =
        Codecs.derive(RedisCodec.Utf8, AccessToken.stringAccessTokenEpi, TokenInfo.stringTokenInfoEpi)

      Redis[I].withOptions(redisConfig.uri, clientOptions, redisCodec)
    }

    def httpClientResource(using Logging.Make[F]): Resource[I, Client[F]] = {
      val logger = Logging.Make[F].forService[Client[F]]

      EmberClientBuilder
        .default[F]
        .build
        .map(clientRequestF =>
          Logger[F](
            logHeaders = true,
            logBody = true,
            logAction = Some((msg: String) => logger.debug(msg))
          )(clientRequestF)
        ).mapK(RequestContext.setupK[I, F])
    }

    for {
      middleware <- Resource.eval(ClientMiddleware.builder[F] {
                      ClientSpanDataProvider
                        .openTelemetry(new OnlyRedactUserInfo {})
                        .optIntoUrlScheme
                        .optIntoUserAgentOriginal
                        .and(AttributeProvider.middlewareVersion)
                    }.build)
                      .mapK(RequestContext.setupK[I, F])
      client <- httpClientResource
      given Client[F] = middleware.wrap(client)
    } yield Clients[F]()
  }
}
