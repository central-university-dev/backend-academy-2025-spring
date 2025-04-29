package tbank.ab

import cats.arrow.FunctionK
import cats.data.ReaderT
import cats.effect.{ExitCode, IO, IOApp}
import cats.effect.kernel.Resource
import cats.implicits.*
import pureconfig.ConfigSource
import tbank.ab.config.{AppConfig, DbConfig, ServerConfig}
import tbank.ab.db.DatabaseModule
import tbank.ab.domain.{RequestContext, RequestIO}
import tbank.ab.wiring.*
import tofu.logging.Logging
import tofu.syntax.logging.*

object Seminar12App extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    application.useForever
      .as(ExitCode.Success)

  // Logging
  given Logging.Make[IO] = Logging.Make.plain[IO]
  given Logging[IO]      = Logging.Make[IO].forService[Seminar12App.type]

  def application: Resource[IO, Unit] =
    for {
      _ <- Resource.make(info"Starting application...")(_ => info"Application closed")
      // Load configs
      given AppConfig <- AppConfig.load(ConfigSource.default).toResource
      given DbConfig     = summon[AppConfig].database
      given ServerConfig = summon[AppConfig].server

      // Connect to db
      given DatabaseModule[IO] = DatabaseModule.make[IO]
      _ <- LiquibaseMigration.run[IO]().toResource

      given FunctionK[IO, RequestIO] = ReaderT.liftK[IO, RequestContext]

      // Create wiring
      given Clients[IO]      <- Clients.make[IO]
      given Repositories[IO] <- Repositories.make[IO].toResource
      given Services[IO]     <- Resource.eval(Services.make[IO])
      given PublicApi[IO]     = PublicApi.make[IO]
      given MonitoringApi[IO] = MonitoringApi.make[IO]

      // Start server and consumer
      _ <- HttpServer.startServer[IO]

      _ <- Resource.make(info"Application started")(_ => info"Closing application...")
    } yield ()
}
