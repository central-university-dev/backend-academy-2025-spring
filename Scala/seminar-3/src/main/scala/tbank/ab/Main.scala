package tbank.ab

import cats.effect.{ExitCode, IO, IOApp}
import com.comcast.ip4s.{Host, Port}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import sttp.capabilities.WebSockets
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.http4s.Http4sServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter
import tbank.ab.config.AppConfig
import tbank.ab.controller.*
import tbank.ab.wiring.{Repositories, Services}

object Main extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    for {
      config       <- AppConfig.load
      repositories <- Repositories.make(config)
      services = Services.make(config, repositories)
      endpoints: List[ServerEndpoint[Fs2Streams[IO] & WebSockets, IO]] <-
        IO {
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
            )
            /*ChatController.make(
              services.chatService
            )*/
          ).flatMap(_.endpoints)
        }

      swagger = SwaggerInterpreter()
                  .fromServerEndpoints[IO](endpoints, "seminar-1", "1.0.0")

      routes = Http4sServerInterpreter[IO]()
                 .toWebSocketRoutes(swagger ++ endpoints)

      port <- getPortSafe(config.port)
      _ <-
        EmberServerBuilder
          .default[IO]
          .withHost(Host.fromString("localhost").get)
          .withPort(port)
          .withHttpWebSocketApp(ws => Router("/" -> routes(ws)).orNotFound)
          .build
          .evalTap(server =>
            IO.println(
              s"Swagger available at http://localhost:${port.value}/docs"
            )
          )
          .useForever
    } yield ExitCode.Success

  private def getPortSafe(v: Int): IO[Port] =
    IO.delay(
      Port.fromInt(v).toRight(IllegalArgumentException("Invalid port value"))
    ).rethrow
