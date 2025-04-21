package tbank.ab

import cats.effect.Async
import cats.effect.std.Console
import cats.implicits.catsSyntaxFlatMapOps
import doobie.FC
import doobie.implicits.toConnectionIOOps
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import tbank.ab.db.DatabaseModule

object LiquibaseMigration {

  def run[F[_]: Async: Console](
    changeLog: String = "changelog.xml"
  )(using database: DatabaseModule[F]): F[Unit] =
    Console[F].println("Starting liquibase migration...") >>
    FC.raw { conn =>
      val resourceAccessor = new ClassLoaderResourceAccessor(getClass.getClassLoader)
      val database         = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(conn))
      val liquibase        = new Liquibase(changeLog, resourceAccessor, database)
      liquibase.update("") // TODO: Update deprecated method
    }.transact(database.transactor) >>
    Console[F].println("Migration finished.")

}
