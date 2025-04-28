package tbank.ab.wiring

import cats.{~>, Applicative, Monad}
import cats.data.{Kleisli, ReaderT}
import cats.effect.{Async, Resource}
import cats.implicits.*
import com.comcast.ip4s.{Host, Port}
import fs2.io.net.Network
import org.http4s.{HttpRoutes, Request, Response}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.{Router, Server}
import org.http4s.server.websocket.WebSocketBuilder2
import sttp.apispec.openapi.circe.yaml.RichOpenAPI
import sttp.capabilities.WebSockets
import sttp.capabilities.fs2.Fs2Streams
import sttp.client3.impl.cats.implicits.asyncMonadError
import sttp.monad.MonadError
import sttp.tapir.docs.openapi.{OpenAPIDocsInterpreter, OpenAPIDocsOptions}
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.SwaggerUI
import tbank.ab.config.{ServerConfig, ZoneConfig}
import tbank.ab.domain.RequestContext
import tofu.WithProvide
import tofu.generate.GenUUID
import tofu.logging.Logging
import tofu.syntax.logging.*

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

  private def registerZone[F[_]: Async](
    endpoints: List[ServerEndpoint[Fs2Streams[F] & WebSockets, F]],
    config: ZoneConfig
  ): Resource[F, Server] =
    EmberServerBuilder.default[F]
      .withPort(Port.fromInt(config.port).get)
      .withHostOption(Host.fromString(config.host))
      .withHttpWebSocketApp(ws =>
        Router("/" -> Http4sServerInterpreter[F]().toWebSocketRoutes(endpoints)(ws)).orNotFound
      )
      .build

  def registerZoneWithLogging[F[_]: Async](
    endpoints: List[ServerEndpoint[Any, F]],
    config: ZoneConfig
  ): Resource[F, Server] = {
    
    EmberServerBuilder.default[F]
      .withPort(Port.fromInt(config.port).get)
      .withHostOption(Host.fromString(config.host))
      .withHttpWebSocketApp { ws =>
        Router("/" -> Http4sServerInterpreter[F]().toWebSocketRoutes(endpoints)(ws)).orNotFound
      }
      .build
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
      public     <- registerZoneWithLogging[F](publicEndpoints, conf.public)
      monitoring <- registerZone[F](monitoringEndpoints, conf.monitoring)
      _          <- Resource.make(info"Server started")(_ => info"Closing server...")

      _ <- Resource.eval(logZoneUri[F]("public", public, conf.public))
      _ <- Resource.eval(logZoneUri[F]("monitoring", monitoring, conf.monitoring))
    } yield HttpServer(public, monitoring)
  }
}
