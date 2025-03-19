package tbank.ab

import cats.effect.{ExitCode, IO, IOApp}
import cats.effect.kernel.Resource
import com.comcast.ip4s.Port
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.{Router, Server}
import pureconfig.ConfigSource
import sttp.apispec.asyncapi.circe.yaml.*
import sttp.capabilities.WebSockets
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.AnyEndpoint
import sttp.tapir.docs.asyncapi.AsyncAPIInterpreter
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import tbank.ab.config.AppConfig
import tbank.ab.controller.*
import tbank.ab.wiring.{Repositories, Services}

object Seminar4App extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    application.useForever
      .as(ExitCode.Success)

  def application: Resource[IO, Server] =
    for {
      _            <- Resource.make(IO.println("Starting application..."))(_ => IO.println("Application closed"))
      config       <- AppConfig.load(ConfigSource.default).toResource
      repositories <- Repositories.make(config).toResource
      services = Services.make(config, repositories)
      endpoints: List[ServerEndpoint[Fs2Streams[IO] & WebSockets, IO]] =
        List(
          ProbeController.make,
          AuthController.make(services.authService, config.auth),
          AnimalController.make(
            services.animalService,
            services.authService
          ),
          HabitatController.make(
            services.habitatService,
            services.authService
          ),
          ChatController.make(
            services.chatService
          )
        ).flatMap(_.endpoints)

      swagger = SwaggerInterpreter()
                  .fromServerEndpoints[IO](endpoints, "seminar-1", "1.0.0")
      asyncApi = AsyncAPIInterpreter()
                   .toAsyncAPI(endpoints.map(_.endpoint: AnyEndpoint), "Chat web socket", "1.0")

      routes = Http4sServerInterpreter[IO]()
                 .toWebSocketRoutes(swagger ++ endpoints)

      port <- getPortSafe(config.port).toResource
      server <-
        EmberServerBuilder
          .default[IO]
          .withoutHost
          .withPort(port)
          .withHttpWebSocketApp(ws => Router("/" -> routes(ws)).orNotFound)
          .build
          .evalTap(server =>
            IO.println(asyncApi.toYaml) *>
            IO.println(
              s"Swagger available at http://localhost:${server.address.getPort}/docs"
            )
          )
      _ <- Resource.make(IO.println("Application started"))(_ => IO.println("Closing application..."))
    } yield server

  private def getPortSafe(v: Int): IO[Port] =
    IO.delay(
      Port.fromInt(v).toRight(IllegalArgumentException("Invalid port value"))
    ).rethrow
