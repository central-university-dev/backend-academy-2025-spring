package tbank.ab.wiring

import cats.effect.{IO, Resource}
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

import java.net.URI
import java.time.Duration

case class Clients()(using
  val s3Client: S3[IO],
  val redisClient: RedisCommands[IO, AccessToken, TokenInfo],
  val httpClient: Client[IO]
)

object Clients {

  private def s3StreamResource(using s3Config: S3Config): Resource[IO, S3[IO]] = {
    val credentials = AwsBasicCredentials.create(s3Config.accessKey, s3Config.secretKey)

    Interpreter[IO].S3AsyncClientOpResource(
      S3AsyncClient
        .builder()
        .forcePathStyle(true)
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .endpointOverride(URI.create(s3Config.uri))
        .region(Region.US_EAST_1)
    ).map(S3.create[IO](_))
  }

  private def redisResource(using redisConfig: RedisConfig): Resource[IO, RedisCommands[IO, AccessToken, TokenInfo]] = {
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

    Redis[IO].withOptions(redisConfig.uri, clientOptions, redisCodec)
  }

  private def httpClientResource: Resource[IO, Client[IO]] = {
    EmberClientBuilder
      .default[IO]
      .build
  }

  def make(using config: AppConfig): Resource[IO, Clients] = {
    import config.given

    for {
      given S3[IO]                                    <- s3StreamResource
      given RedisCommands[IO, AccessToken, TokenInfo] <- redisResource
      given Client[IO] <- httpClientResource
    } yield Clients()
  }
}
