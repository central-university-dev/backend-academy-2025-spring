package tbank.ab.wiring

import cats.effect.{Async, Resource}
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
import scala.concurrent.duration.*

case class Clients[F[_]]()(using
  val httpClient: Client[F]
)

object Clients {

  def s3StreamResource[I[_]: Async](using s3Config: S3Config): Resource[I, S3[I]] = {
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

  def redisResource[I[_]: Async](using
    redisConfig: RedisConfig
  ): Resource[I, RedisCommands[I, AccessToken, TokenInfo]] = {
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

  def httpClientResource[F[_]: Async]: Resource[F, Client[F]] =
    EmberClientBuilder
      .default[F]
      .build

  def loggingHttpClientResource[I[_]: Async, F[_]: Async](using
    make: Logging.Make[F],
    withProvide: WithProvide[F, I, RequestContext]
  ): Resource[I, Client[F]] = {
    val logger = Logging.Make[F].forService[Client[F]]

    httpClientResource[F]
      .map(clientRequestF =>
        Logger[F](
          logHeaders = true,
          logBody = true,
          logAction = Some((msg: String) => logger.debug(msg))
        )(clientRequestF)
      ).mapK(RequestContext.setupK[I, F])
  }

  def make[I[_]: Async, F[_]: Async](using
    config: AppConfig,
    make: Logging.Make[F],
    withProvide: WithProvide[F, I, RequestContext]
  ): Resource[I, Clients[F]] = {
    import config.given

    for {
      given Client[F] <- loggingHttpClientResource[I, F]
    } yield Clients[F]()
  }
}
