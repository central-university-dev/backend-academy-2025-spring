package tbank.ab.wiring

import cats.syntax.all.*
import cats.effect.{Async, Resource}
import dev.profunktor.redis4cats.{Redis, RedisCommands}
import dev.profunktor.redis4cats.codecs.Codecs
import dev.profunktor.redis4cats.data.*
import dev.profunktor.redis4cats.effect.Log.NoOp.instance
import fs2.aws.s3.S3
import io.chrisdavenport.circuit.CircuitBreaker
import io.laserdisc.pure.s3.tagless.Interpreter
import io.lettuce.core.{ClientOptions, TimeoutOptions}
import org.http4s.{Request, Response, Status}
import org.http4s.Status.ImATeapot
import org.http4s.client.{Client, ConnectionFailure}
import org.http4s.client.middleware.{Logger, Retry, RetryPolicy}
import org.http4s.ember.client.EmberClientBuilder
import org.typelevel.log4cats.syntax.LoggerInterpolator
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
import scala.util.control.NoStackTrace

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


  def httpClientResource[F[_] : Async](using  make: Logging.Make[F]): Resource[F, Client[F]] = {

    def isRetriable(req: Request[F], result: Either[Throwable, Response[F]]): Boolean =
      req.isIdempotent &&
        (result match {
          case Left(_: ConnectionFailure) => true
          case Right(res) if RetryPolicy.RetriableStatuses.contains(res.status) => true
          case _ => false
        })

    val policy = RetryPolicy[F](backoff = RetryPolicy.exponentialBackoff(500.millis, 4), isRetriable)

    EmberClientBuilder
      .default[F]
      .withTimeout(500.millis)
      .build
      .map(client =>
        Retry(policy)(client)
      ).evalMap(client =>
        circuitBreakerMiddle[F](client)
      )
  }

  case class FailedHttpStatusException[F[_]](response: Response[F], release: F[Unit]) extends Exception with NoStackTrace

  private def circuitBreakerMiddle[F[_] : Async](client: Client[F])(using make: Logging.Make[F]): F[Client[F]] = {
    val logging: Logging[F] = make.forService[CircuitBreaker[F]]
    
    CircuitBreaker.default[F](
        maxFailures = 2,
        resetTimeout = 1.seconds)
      .withOnOpen(logging.info("CircuitBreaker is open"))
      .withOnHalfOpen(logging.info("CircuitBreaker is half-open"))
      .withOnClosed(logging.info("CircuitBreaker is closed"))
      .build
      .map { circuitBreaker =>
        Client[F] { request =>
          Resource.apply[F, Response[F]](
            circuitBreaker.protect(
              client.run(request).allocated
            )
          )
        }
      }
  }

//  private def circuitBreakerMiddle[F[_] : Async](client: Client[F]): F[Client[F]] = {
//    CircuitBreaker.default[F](
//        maxFailures = 2,
//        resetTimeout = 2.seconds)
//      .build
//      .map { circuitBreaker =>
//        Client[F] { request =>
//          Resource.apply[F, Response[F]](
//            circuitBreaker.protect(
//              client.run(request).allocated
//                              .flatTap { case (res, release) =>
//                              if (res.status == Status.InsufficientStorage)
//                                Async[F].raiseError(FailedHttpStatusException[F](res, release))
//                              else res.pure
//                            }
//            ).recover { case err: FailedHttpStatusException[F] =>
//              (err.response, err.release)
//            }
//          )
//        }
//      }
//  }


  def loggingHttpClientResource[F[_]: Async](using
    make: Logging.Make[F],
  ): Resource[F, Client[F]] = {
    val logger = Logging.Make[F].forService[Client[F]]

    httpClientResource[F]
      .map(clientRequestF =>
        Logger[F](
          logHeaders = true,
          logBody = true,
          logAction = Some((msg: String) => logger.debug(msg))
        )(clientRequestF)
      )
  }

  def make[ F[_]: Async](using
    config: AppConfig,
    make: Logging.Make[F],
  ): Resource[F, Clients[F]] = {
    import config.given

    for {
      given Client[F] <- loggingHttpClientResource[F]
    } yield Clients[F]()
  }
}
