package tbank.ab

import cats.effect.IO
import cats.effect.std.Console
import doobie.FC
import doobie.implicits.toConnectionIOOps
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import tbank.ab.db.DatabaseModule

object LiquibaseMigration {

  def run(
    changeLog: String = "changelog.xml"
  )(using database: DatabaseModule): IO[Unit] =
    Console[IO].println("Starting liquibase migration...") >>
    FC.raw { conn =>
      val resourceAccessor = new ClassLoaderResourceAccessor(getClass.getClassLoader)
      val database         = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(conn))
      val liquibase        = new Liquibase(changeLog, resourceAccessor, database)
      liquibase.update("") // TODO: Update deprecated method
    }.transact(database.transactor) >>
    Console[IO].println("Migration finished.")

}
