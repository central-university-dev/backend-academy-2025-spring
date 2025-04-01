package tbank.ab

import cats.effect.{ExitCode, IO, IOApp}
import cats.effect.kernel.Resource
import pureconfig.ConfigSource
import tbank.ab.config.{AppConfig, DbConfig, ServerConfig}
import tbank.ab.db.DatabaseModule
import tbank.ab.wiring.{Clients, HttpServer, MonitoringApi, PublicApi, Repositories, Services}

object Seminar7App extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    application.useForever
      .as(ExitCode.Success)

  def application: Resource[IO, HttpServer] =
    for {
      _ <- Resource.make(IO.println("Starting application..."))(_ => IO.println("Application closed"))
      // Load configs
      given AppConfig <- AppConfig.load(ConfigSource.default).toResource
      given DbConfig     = summon[AppConfig].database
      given ServerConfig = summon[AppConfig].server

      // Connect to db
      given DatabaseModule = DatabaseModule.make
      _ <- LiquibaseMigration.run().toResource

      // Create wiring
      given Clients      <- Clients.make
      given Repositories <- Repositories.make.toResource
      given Services      = Services.make
      given MonitoringApi = MonitoringApi.make
      given PublicApi     = PublicApi.make

      // Start server
      server <- HttpServer.startServer

      _ <- Resource.make(IO.println("Application started"))(_ => IO.println("Closing application..."))
    } yield server
}
