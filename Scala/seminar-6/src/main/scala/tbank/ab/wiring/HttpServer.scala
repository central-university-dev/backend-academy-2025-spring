package tbank.ab.wiring

import cats.effect.{IO, Resource}
import com.comcast.ip4s.{Host, Port}
import fs2.io.net.Network
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

final case class HttpServer private (
  public: Server,
  monitoring: Server
)

object HttpServer {

  private def withDocs[R](
    endpoints: List[ServerEndpoint[R, IO]],
    config: ZoneConfig
  ): List[ServerEndpoint[R, IO]] = {
    val openApi: String =
      OpenAPIDocsInterpreter(OpenAPIDocsOptions.default)
        .toOpenAPI(es = endpoints.map(_.endpoint), config.name, "1.0")
        .toYaml

    if (config.swaggerEnabled) endpoints ::: SwaggerUI[IO](openApi) else endpoints
  }

  private def registerZone(
    endpoints: List[ServerEndpoint[Fs2Streams[IO] & WebSockets, IO]],
    config: ZoneConfig
  ): Resource[IO, Server] =
    EmberServerBuilder.default[IO]
      .withPort(Port.fromInt(config.port).get)
      .withHostOption(Host.fromString(config.host))
      .withHttpWebSocketApp(ws =>
        Router("/" -> Http4sServerInterpreter[IO]().toWebSocketRoutes(endpoints)(ws)).orNotFound
      )
      .build

  private def logZoneUri(zone: String, server: Server, conf: ZoneConfig): IO[Unit] =
    IO.println(s"$zone zone started at ${server.baseUri.renderString}") >>
    IO.whenA(conf.swaggerEnabled)(IO.println(s"SSwagger UI for $zone zone available at ${server.baseUri.renderString}/docs"))

  def startServer(using
    publicApi: PublicApi,
    monitoringApi: MonitoringApi,
    conf: ServerConfig
  ): Resource[IO, HttpServer] = {
    val publicEndpoints     = withDocs(publicApi.endpoints, conf.public)
    val monitoringEndpoints = withDocs(monitoringApi.endpoints, conf.monitoring)

    for {
      _          <- Resource.make(IO.println("Starting server..."))(_ => IO.println("Server closed"))
      public     <- registerZone(publicEndpoints, conf.public)
      monitoring <- registerZone(monitoringEndpoints, conf.monitoring)
      _          <- Resource.make(IO.println("Server started"))(_ => IO.println("Closing server..."))

      _ <- logZoneUri("public", public, conf.public).toResource
      _ <- logZoneUri("monitoring", monitoring, conf.monitoring).toResource
    } yield HttpServer(public, monitoring)
  }

}
