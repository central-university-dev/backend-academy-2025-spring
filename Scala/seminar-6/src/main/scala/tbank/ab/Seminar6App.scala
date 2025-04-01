package tbank.ab

import cats.effect.{ExitCode, IO, IOApp}
import cats.effect.kernel.Resource
import pureconfig.ConfigSource
import tbank.ab.config.{AppConfig, DbConfig, ServerConfig}
import tbank.ab.db.DatabaseModule
import tbank.ab.wiring.{HttpServer, MonitoringApi, PublicApi, Repositories, Services}

// docker run --name my-postgres -e POSTGRES_PASSWORD=password -e POSTGRES_USER=user -e POSTGRES_DB=mydb -d -p 5432:5432 postgres:alpine
// Конфиг будет выглядеть так:
//  driver = "org.postgresql.Driver"
//  url = "jdbc:postgresql://localhost:5432/mydb"
//  user = "user"
//  password = "password"
object Seminar6App extends IOApp {
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
      given Repositories <- Repositories.make.toResource
      given Services      = Services.make
      given MonitoringApi = MonitoringApi.make
      given PublicApi     = PublicApi.make

      // Start server
      server <- HttpServer.startServer

      _ <- Resource.make(IO.println("Application started"))(_ => IO.println("Closing application..."))
    } yield server
}
