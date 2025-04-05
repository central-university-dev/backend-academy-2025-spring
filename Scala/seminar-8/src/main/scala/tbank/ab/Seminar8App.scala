package tbank.ab

import cats.effect.{ExitCode, IO, IOApp}
import cats.effect.kernel.Resource
import cats.implicits._
import pureconfig.ConfigSource
import tbank.ab.config.{AppConfig, DbConfig, ServerConfig}
import tbank.ab.db.DatabaseModule
import tbank.ab.wiring.{Clients, HttpServer, MonitoringApi, PublicApi, Repositories, Services, Producers}
import tbank.ab.mq.AnimalConsumer
import tbank.ab.config.KafkaConsumerConfig
import tbank.ab.service.AnimalService

object Seminar8App extends IOApp {
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
      given KafkaConsumerConfig = summon[AppConfig].animalsConsumer

      // Connect to db
      given DatabaseModule = DatabaseModule.make
      _ <- LiquibaseMigration.run().toResource

      // Create wiring
      given Clients      <- Clients.make
      given Repositories <- Repositories.make.toResource
      given Producers    <- Producers.make
      given Services      = Services.make
      given MonitoringApi = MonitoringApi.make
      given PublicApi     = PublicApi.make

      // Start server and consumer
      _ <- List(HttpServer.startServer, AnimalConsumer.run).parSequence

      _ <- Resource.make(IO.println("Application started"))(_ => IO.println("Closing application..."))
    } yield ()
}
