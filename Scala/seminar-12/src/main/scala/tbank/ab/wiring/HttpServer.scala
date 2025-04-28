package tbank.ab.wiring

import cats.Monad
import cats.effect.{Async, Resource}
import cats.effect.std.Backpressure
import cats.implicits.*
import com.comcast.ip4s.{Host, Port}
import fs2.io.net.Network
import org.http4s.{HttpRoutes, Response}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.{Router, Server}
import org.http4s.server.middleware.Throttle
import sttp.apispec.openapi.circe.yaml.RichOpenAPI
import sttp.tapir.docs.openapi.{OpenAPIDocsInterpreter, OpenAPIDocsOptions}
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.SwaggerUI
import tbank.ab.config.{ServerConfig, ZoneConfig}
import tofu.logging.Logging
import tofu.syntax.logging.*

import scala.concurrent.duration.*

final case class HttpServer private (
  public: Server,
  monitoring: Server
)

object HttpServer {

  private def withDocs[R, F[_]](
    endpoints: List[ServerEndpoint[R, F]],
    config: ZoneConfig
  ): List[ServerEndpoint[R, F]] = {
    val openApi: String =
      OpenAPIDocsInterpreter(OpenAPIDocsOptions.default)
        .toOpenAPI(es = endpoints.map(_.endpoint), config.name, "1.0")
        .toYaml

    if (config.swaggerEnabled) endpoints ::: SwaggerUI[F](openApi) else endpoints
  }

  def registerZone[F[_]: Async](
    endpoints: List[ServerEndpoint[Any, F]],
    config: ZoneConfig
  ): Resource[F, Server] = {

    val routes = Router("/" -> Http4sServerInterpreter[F]().toRoutes(endpoints))

    val throttleAppF = Throttle.httpApp[F](
      amount = 10,
      per = 1.minute
    )(routes.orNotFound)

    Resource.eval(Backpressure[F](Backpressure.Strategy.Lossy, 1)).flatMap { backpressure =>
      EmberServerBuilder.default[F]
        .withPort(Port.fromInt(config.port).get)
        .withHostOption(Host.fromString(config.host))
        .withHttpApp(
          RateLimiterMiddle.create(routes, backpressure).orNotFound
        )
        .build
    }
  }

  private def logZoneUri[F[_]: Monad](zone: String, server: Server, conf: ZoneConfig)(using
    logging: Logging[F]
  ): F[Unit] =
    info"$zone zone started at ${server.baseUri.renderString}" >>
    Monad[F].whenA(conf.swaggerEnabled)(
      info"SSwagger UI for $zone zone available at ${server.baseUri.renderString}/docs"
    )

  def startServer[F[_]: Async](using
    publicApi: PublicApi[F],
    monitoringApi: MonitoringApi[F],
    conf: ServerConfig,
    make: Logging.Make[F]
  ): Resource[F, HttpServer] = {
    val publicEndpoints     = withDocs(publicApi.endpoints, conf.public)
    val monitoringEndpoints = withDocs(monitoringApi.endpoints, conf.monitoring)

    given Logging[F] = make.forService[HttpServer.type]

    for {
      _          <- Resource.make(info"Starting server...")(_ => info"Server closed")
      public     <- registerZone[F](publicEndpoints, conf.public)
      monitoring <- registerZone[F](monitoringEndpoints, conf.monitoring)
      _          <- Resource.make(info"Server started")(_ => info"Closing server...")

      _ <- Resource.eval(logZoneUri[F]("public", public, conf.public))
      _ <- Resource.eval(logZoneUri[F]("monitoring", monitoring, conf.monitoring))
    } yield HttpServer(public, monitoring)
  }
}
