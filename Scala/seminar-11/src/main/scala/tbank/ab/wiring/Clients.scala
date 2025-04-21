package tbank.ab.wiring

import cats.data.ReaderT
import cats.effect.{Async, Resource}
import cats.~>
import dev.profunktor.redis4cats.{Redis, RedisCommands}
import dev.profunktor.redis4cats.codecs.Codecs
import dev.profunktor.redis4cats.data.*
import dev.profunktor.redis4cats.effect.Log.NoOp.instance
import fs2.aws.s3.S3
import io.laserdisc.pure.s3.tagless.Interpreter
import io.lettuce.core.{ClientOptions, TimeoutOptions}
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient
import tbank.ab.config.{AppConfig, RedisConfig, S3Config}
import tbank.ab.domain.auth.{AccessToken, TokenInfo}
import tofu.logging.Logging
import org.http4s.client.middleware.Logger
import tbank.ab.domain.{RequestContext, RequestIO, SetupContext}
import tofu.WithContext

import java.net.URI
import java.time.Duration

case class Clients[F[_]]()(using
                           val s3Client: S3[F],
                           val redisClient: RedisCommands[F, AccessToken, TokenInfo],
                           val httpClient: Client[F]
)

object Clients {

  def make[I[_]: Async, F[_] : Async](using config: AppConfig, logMake: Logging.Make[F]): Resource[I, Clients[F]] = {
    import config.given
    
    val setup = new SetupContext[I, F]()
    
    def s3StreamResource(using s3Config: S3Config): Resource[I, S3[F]] = {
      val credentials = AwsBasicCredentials.create(s3Config.accessKey, s3Config.secretKey)
  
      Interpreter[F].S3AsyncClientOpResource(
        S3AsyncClient
          .builder()
          .forcePathStyle(true)
          .credentialsProvider(StaticCredentialsProvider.create(credentials))
          .endpointOverride(URI.create(s3Config.uri))
          .region(Region.US_EAST_1)
        ).mapK(setup.setupK).map(S3.create[F])
    }

    def redisResource(using redisConfig: RedisConfig): Resource[I, RedisCommands[F, AccessToken, TokenInfo]] = {
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

      Redis[F].withOptions(redisConfig.uri, clientOptions, redisCodec).mapK(setup.setupK)
  }
    
    def httpClientResource(using Logging.Make[F]): Resource[I, Client[F]] = {
        val logger = Logging.Make[F].forService[Client[F]]
      
          EmberClientBuilder
            .default[F]
            .build
            .map(clientRequestF =>
              Logger[F](
                logHeaders = false,
                logBody = true,
                logAction = Some((msg: String) => logger.debug(msg))
              )(clientRequestF)
            ).mapK(setup.setupK)
    }
    
    for {
      given S3[F] <- s3StreamResource
      given RedisCommands[F, AccessToken, TokenInfo] <- redisResource
      given Client[F] <- httpClientResource
    } yield Clients[F]()
  }
}
