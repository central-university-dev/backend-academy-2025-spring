package tbank.ab.db

import cats.effect.{Async, MonadCancelThrow, Resource}
import cats.~>
import doobie.Transactor
import doobie.hikari.HikariTransactor
import tbank.ab.config.DbConfig

case class DatabaseModule[F[_]](
  transactor: Transactor[F]
                               )

object DatabaseModule {

  def mapK[F[_]: MonadCancelThrow, G[_]: MonadCancelThrow](fa: DatabaseModule[F])(fk: F ~> G): DatabaseModule[G] =
    DatabaseModule(fa.transactor.mapK(fk))


  def make[F[_]](using config: DbConfig, F: Async[F]): DatabaseModule[F] =
    DatabaseModule(
      defaultTransactor[F](config)
    )

  def makeHikari[F[_]](using config: DbConfig, F: Async[F]): Resource[F, DatabaseModule[F]] =
    hikariTransactor[F](config)
      .map(DatabaseModule(_))


  def makeH2[F[_]: Async]: DatabaseModule[F] =
    make[F](using h2DbConf)

  def makeH2Hikari[F[_]: Async]: Resource[F, DatabaseModule[F]] =
    makeHikari[F](using h2DbConf)

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
