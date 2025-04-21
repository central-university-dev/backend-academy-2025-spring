package tbank.ab.wiring

import cats.Monad
import cats.data.Kleisli
import cats.effect.{Resource, Sync, Async}
import com.comcast.ip4s.{Host, Port}
import fs2.io.net.Network
import org.http4s.{HttpRoutes, Request}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.{Router, Server}
import sttp.apispec.openapi.circe.yaml.RichOpenAPI
import sttp.capabilities.WebSockets
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.docs.openapi.{OpenAPIDocsInterpreter, OpenAPIDocsOptions}
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.SwaggerUI
import tbank.ab.config.{ServerConfig, ZoneConfig}
import tbank.ab.domain.{RequestContext, RequestIO}
import tofu.syntax.context.runContext

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

  private def registerZone[I[_]: Async, F[_]: Async](
    endpoints: List[ServerEndpoint[Fs2Streams[F] & WebSockets, F]],
    config: ZoneConfig
  ): Resource[I, Server] =
    EmberServerBuilder.default[I]
      .withPort(Port.fromInt(config.port).get)
      .withHostOption(Host.fromString(config.host))
      .withHttpWebSocketApp(ws =>
        loggingMiddleware(
          Router("/" -> Http4sServerInterpreter[F]().toWebSocketRoutes(endpoints)(ws))
        ).orNotFound
      )
      .build

  def loggingMiddleware[F[_]](service: HttpRoutes[F]): HttpRoutes[F] = {
    Kleisli { (req: Request[F]) =>
      val traceId = java.util.UUID.randomUUID().toString
      val context = RequestContext(traceId)

//      runContext[RequestF](service(req))(context)
      service(req)
    }
  }

//  private def logZoneUri[F[_]: Monad](zone: String, server: Server, conf: ZoneConfig): F[Unit] =
//    F.println(s"$zone zone started at ${server.baseUri.renderString}") >>
//    Monad[F].whenA(conf.swaggerEnabled)(
//      F.println(s"SSwagger UI for $zone zone available at ${server.baseUri.renderString}/docs")
//    )

  def startServer[I[_], F[_]: Async](using
    publicApi: PublicApi[F],
    monitoringApi: MonitoringApi[I],
    conf: ServerConfig
  ): Resource[I, HttpServer] = {
    val publicEndpoints     = withDocs(publicApi.endpoints, conf.public)
    val monitoringEndpoints = withDocs(monitoringApi.endpoints, conf.monitoring)

    for {
//      _          <- Resource.make(IO.println("Starting server..."))(_ => F.println("Server closed"))
      public     <- registerZone(publicEndpoints, conf.public)
      monitoring <- registerZone(monitoringEndpoints, conf.monitoring)
//      _          <- Resource.make(F.println("Server started"))(_ => F.println("Closing server..."))

//      _ <- Resource.eval(logZoneUri("public", public, conf.public))
//      _ <- Resource.eval(logZoneUri("monitoring", monitoring, conf.monitoring))
    } yield HttpServer(public, monitoring)
  }

}
