package tbank.ab.db

import cats.effect.{Async, IO, Resource}
import doobie.Transactor
import doobie.hikari.HikariTransactor
import tbank.ab.config.DbConfig

case class DatabaseModule(
  transactor: Transactor[IO]
)

object DatabaseModule {

  def make(using config: DbConfig): DatabaseModule =
    DatabaseModule(
      defaultTransactor[IO](config)
    )

  def makeHikari(using config: DbConfig): Resource[IO, DatabaseModule] =
    hikariTransactor[IO](config)
      .map(DatabaseModule(_))

  def makeH2: DatabaseModule =
    make(using h2DbConf)

  def makeH2Hikari: Resource[IO, DatabaseModule] =
    makeHikari(using h2DbConf)

  private val h2DbConf: DbConfig = DbConfig(
    driver = "org.h2.Driver",
    url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
    user = "h2",
    password = "qwerty"
  )

  private def defaultTransactor[F[_]: Async](
    config: DbConfig
  ): Transactor[F] =
    Transactor.fromDriverManager[F](
      driver = config.driver,
      url = config.url,
      user = config.user,
      password = config.password,
      logHandler = None
    )

  // example how to use with hikari pool
  private def hikariTransactor[F[_]: Async](
    config: DbConfig
  ): Resource[F, Transactor[F]] =
    HikariTransactor.fromHikariConfig[F](
      DbConfig.toHikariConf(config)
    )
}
