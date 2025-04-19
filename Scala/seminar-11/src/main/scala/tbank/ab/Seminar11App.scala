package tbank.ab

import cats.effect.{ExitCode, IO, IOApp}
import cats.effect.kernel.Resource
import cats.implicits.*
import pureconfig.ConfigSource
import tbank.ab.config.{AppConfig, DbConfig, ServerConfig}
import tbank.ab.db.DatabaseModule
import tbank.ab.wiring.{Clients, HttpServer, MonitoringApi, Producers, PublicApi, Repositories, Services}
import tbank.ab.mq.AnimalConsumer
import tbank.ab.config.KafkaConsumerConfig
import tbank.ab.service.AnimalService
import tofu.logging.Logging
import tofu.syntax.logging.*

object Seminar11App extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    application.useForever
      .as(ExitCode.Success)

  def application: Resource[IO, Unit] =
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

      given Logging.Make[IO] = Logging.Make.plain[IO]
      given Logging[IO] = Logging.Make[IO].forService[Seminar11App.type]

      // Start server and consumer
      _ <- HttpServer.startServer

      _ <- Resource.make(info"Application started")(_ => info"Closing application...")
    } yield ()
}
