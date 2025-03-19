package tbank.ab.wiring

import cats.effect.{IO, Resource}
import fs2.aws.s3.S3
import io.laserdisc.pure.s3.tagless.{Interpreter, S3AsyncClientOp}
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient
import tbank.ab.config.{AppConfig, S3Config}

import java.net.URI

case class Clients()(using
  val s3Client: S3[IO]
)

object Clients {

  private def s3StreamResource(using s3Config: S3Config): Resource[IO, S3[IO]] = {
    val credentials = AwsBasicCredentials.create(s3Config.accessKey, s3Config.secretKey)

    Interpreter[IO].S3AsyncClientOpResource(
      S3AsyncClient
        .builder()
        .credentialsProvider(StaticCredentialsProvider.create(credentials))
        .endpointOverride(URI.create(s3Config.uri))
        .region(Region.US_EAST_1)
    ).map(S3.create[IO](_))
  }

  def make(using config: AppConfig): Resource[IO, Clients] = {
    import config.given

    for {
      given S3[IO] <- s3StreamResource
    } yield Clients()
  }
}
