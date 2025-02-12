package tbank.ab

import cats.effect.{ExitCode, IO, IOApp}
import com.comcast.ip4s.{Host, Port}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import sttp.tapir.server.http4s.Http4sServerInterpreter
import tbank.ab.config.AppConfig
import tbank.ab.controller.{
  AnimalController,
  AuthController,
  HabitatController,
  ProbeController
}
import tbank.ab.wiring.{Repositories, Services}

object Main extends IOApp:
  override def run(args: List[String]): IO[ExitCode] =
    for {
      config       <- AppConfig.load
      repositories <- Repositories.make(config)
      services = Services.make(config, repositories)
      endpoints <-
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
          ).flatMap(_.endpoints)
        }

      routes = Http4sServerInterpreter[IO]()
                 .toRoutes(endpoints)

      port <- getPortSafe(config.port)
      _ <-
        EmberServerBuilder
          .default[IO]
          .withHost(Host.fromString("localhost").get)
          .withPort(port)
          .withHttpApp(Router("/" -> routes).orNotFound)
          .build
          .evalTap(server =>
            IO.println(
              s"Server available at http://localhost:${server.address.getPort}"
            )
          )
          .useForever
    } yield ExitCode.Success

  private def getPortSafe(v: Int): IO[Port] =
    IO.delay(
      Port.fromInt(v).toRight(IllegalArgumentException("Invalid port value"))
    ).rethrow
