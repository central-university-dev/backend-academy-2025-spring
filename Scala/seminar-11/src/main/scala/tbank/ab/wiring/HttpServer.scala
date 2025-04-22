package tbank.ab.wiring

import cats.Monad
import cats.data.Kleisli
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

  private def registerZone[I[_]: Async](
    endpoints: List[ServerEndpoint[Fs2Streams[I] & WebSockets, I]],
    config: ZoneConfig
  ): Resource[I, Server] =
    EmberServerBuilder.default[I]
      .withPort(Port.fromInt(config.port).get)
      .withHostOption(Host.fromString(config.host))
      .withHttpWebSocketApp(ws =>
        Router("/" -> Http4sServerInterpreter[I]().toWebSocketRoutes(endpoints)(ws)).orNotFound
      )
      .build

  private def registerZoneWithLogging[I[_]: Async, F[_]: Async](
    endpoints: List[ServerEndpoint[Fs2Streams[F] & WebSockets, F]],
    config: ZoneConfig
  )(using withProvide: WithProvide[F, I, RequestContext], genUuid: GenUUID[I]): Resource[I, Server] = {

    def contextMid(service: HttpRoutes[F]): HttpRoutes[I] =
      Kleisli { (req: Request[I]) =>
        val reqF: Request[F] = req.mapK(withProvide.liftF)
        service(reqF).mapK(RequestContext.setupK[I, F]).map(_.mapK(RequestContext.setupK[I, F]))
      }

    EmberServerBuilder.default[I]
      .withPort(Port.fromInt(config.port).get)
      .withHostOption(Host.fromString(config.host))
      .withHttpWebSocketApp { ws =>
        val wsF: WebSocketBuilder2[F] = ws.imapK(withProvide.liftF)(RequestContext.setupK[I, F])
        contextMid(
          Router("/" -> Http4sServerInterpreter[F]().toWebSocketRoutes(endpoints)(wsF))
        ).orNotFound
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

  def startServer[I[_]: Async, F[_]: Async: Logging.Make](using
    publicApi: PublicApi[F],
    monitoringApi: MonitoringApi[I],
    conf: ServerConfig,
    withProvide: WithProvide[F, I, RequestContext],
    make: Logging.Make[I]
  ): Resource[I, HttpServer] = {
    val publicEndpoints     = withDocs(publicApi.endpoints, conf.public)
    val monitoringEndpoints = withDocs(monitoringApi.endpoints, conf.monitoring)

    given Logging[I] = make.forService[HttpServer.type]

    for {
      _          <- Resource.make(info"Starting server...")(_ => info"Server closed")
      public     <- registerZoneWithLogging[I, F](publicEndpoints, conf.public)
      monitoring <- registerZone[I](monitoringEndpoints, conf.monitoring)
      _          <- Resource.make(info"Server started")(_ => info"Closing server...")

      _ <- Resource.eval(logZoneUri[I]("public", public, conf.public))
      _ <- Resource.eval(logZoneUri[I]("monitoring", monitoring, conf.monitoring))
    } yield HttpServer(public, monitoring)
  }

}
