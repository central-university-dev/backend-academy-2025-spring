package tbank.ab.wiring

import cats.arrow.FunctionK
import cats.{Monad, ~>}
import cats.data.{Kleisli, OptionT}
import cats.effect.{Async, Resource, Sync}
import com.comcast.ip4s.{Host, Port}
import cats.implicits.*
import fs2.io.net.Network
import org.http4s.client.Client
import org.http4s.server.middleware.{Logger, RequestLogger}
import org.http4s.{HttpRoutes, Request, Response}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.server.{Router, Server}
import sttp.apispec.openapi.circe.yaml.RichOpenAPI
import sttp.capabilities.WebSockets
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.docs.openapi.{OpenAPIDocsInterpreter, OpenAPIDocsOptions}
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.{Http4sServerInterpreter, serverSentEventsBody}
import sttp.tapir.swagger.SwaggerUI
import tbank.ab.config.{ServerConfig, ZoneConfig}
import tbank.ab.domain.{RequestContext, RequestIO, SetupContext}
import tofu.WithProvide
import tofu.WithRun
import tofu.syntax.context.runContext
import tofu.generate.{GenRandom, GenUUID}
import tofu.lift.Lift
import tofu.logging.Logging
import tofu.syntax.funk.*

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

  private def registerZone[I[_] : Async](
                                          endpoints: List[ServerEndpoint[Fs2Streams[I] & WebSockets, I]],
                                          config: ZoneConfig
                                        ): Resource[I, Server] = {

    EmberServerBuilder.default[I]
      .withPort(Port.fromInt(config.port).get)
      .withHostOption(Host.fromString(config.host))
      .withHttpWebSocketApp(ws =>
        Router("/" -> Http4sServerInterpreter[I]().toWebSocketRoutes(endpoints)(ws)).orNotFound
      )
      .build
  }


  private def registerZone[I[_]: Async, F[_]: Async](
    endpoints: List[ServerEndpoint[Fs2Streams[F] & WebSockets, F]],
    config: ZoneConfig
                                                    )(using withProvide: WithProvide[F, I, RequestContext],
                                                      genUuid: GenUUID[I]
                                                    ): Resource[I, Server] = {

    val fkWithContext = new FunctionK[F, I] {
      override def apply[A](fa: F[A]): I[A] =
        genUuid.randomUUID.flatMap { uuid =>
          val ctx = RequestContext(uuid.toString)
          withProvide.runContext(fa)(ctx)
        }
    }

    // FunctionK[I, F] = withProvide.liftF[I, F]

    def contextMid(service: HttpRoutes[F]): HttpRoutes[I] =
      Kleisli { (req: Request[I]) =>

        val reqF: Request[F] = req.mapK(withProvide.liftF)
        service(reqF).mapK(fkWithContext).map(_.mapK(fkWithContext))
      }

    def routes(ws: WebSocketBuilder2[F]): HttpRoutes[F] = Router("/" -> Http4sServerInterpreter[F]().toWebSocketRoutes(endpoints)(ws))

    def loggingRoutes(ws: WebSocketBuilder2[F]): HttpRoutes[I] = contextMid(routes(ws))


    EmberServerBuilder.default[I]
      .withPort(Port.fromInt(config.port).get)
      .withHostOption(Host.fromString(config.host))
      .withHttpWebSocketApp { ws =>
        val wsF: WebSocketBuilder2[F] = ws.imapK(withProvide.liftF)(fkWithContext)
        contextMid(routes(wsF)).orNotFound
      }
      .build
  }

//  private def logZoneUri[F[_]: Monad](zone: String, server: Server, conf: ZoneConfig): F[Unit] =
//    F.println(s"$zone zone started at ${server.baseUri.renderString}") >>
//    Monad[F].whenA(conf.swaggerEnabled)(
//      F.println(s"SSwagger UI for $zone zone available at ${server.baseUri.renderString}/docs")
//    )

  def startServer[I[_] : Async, F[_] : Async : Logging.Make](using
    publicApi: PublicApi[F],
    monitoringApi: MonitoringApi[I],
                                              conf: ServerConfig,
                                              withProvide: WithProvide[F, I, RequestContext]
  ): Resource[I, HttpServer] = {
    val publicEndpoints     = withDocs(publicApi.endpoints, conf.public)
    val monitoringEndpoints = withDocs(monitoringApi.endpoints, conf.monitoring)

    for {
//      _          <- Resource.make(IO.println("Starting server..."))(_ => F.println("Server closed"))
      public <- registerZone[I, F](publicEndpoints, conf.public)
      monitoring <- registerZone[I](monitoringEndpoints, conf.monitoring)
//      _          <- Resource.make(F.println("Server started"))(_ => F.println("Closing server..."))

//      _ <- Resource.eval(logZoneUri("public", public, conf.public))
//      _ <- Resource.eval(logZoneUri("monitoring", monitoring, conf.monitoring))
    } yield HttpServer(public, monitoring)
  }

}
