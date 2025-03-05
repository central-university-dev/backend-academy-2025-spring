package tbank.ab.repository

import cats.effect.*
import cats.effect.{IO, Ref}
import cats.implicits.*
import doobie.*
import doobie.Transactor
import doobie.h2.*
import doobie.implicits.*
import doobie.implicits.toSqlInterpolator
import tbank.ab.config.AppConfig
import tbank.ab.db.DatabaseModule
import tbank.ab.domain.animal.{AnimalId, AnimalInfo}
import tbank.ab.domain.habitat.Habitat
import tbank.ab.repository.model.AnimalInfoRepository

trait AnimalRepository[F[_]] {
  def find(id: AnimalId): F[Option[AnimalInfo]]
  def update(id: AnimalId, info: AnimalInfo): F[AnimalInfo]
}

object AnimalRepository {

  private class Impl(
    transactor: Transactor[IO]
  ) extends AnimalRepository[IO] {
    override def find(id: AnimalId): IO[Option[AnimalInfo]] =
      sql"""select a.id, name, description, val, domesticated_year from animal_info as a
           left join habitat as h on habitat_id = h.id where name = ${id.toString}"""
        .query[AnimalInfoRepository]
        .option
        .transact[IO](transactor)
        .map(
          _.map(info =>
            AnimalInfo(
              description = info.description,
              habitat = info.habitat, // fix me
              features = List.empty,
              domesticatedYear = info.domesticatedYear,
              voice = None
            )
          )
        )

    override def update(id: AnimalId, info: AnimalInfo): IO[AnimalInfo] =
      sql"""update animal_info set
            description = ${info.description},
            domesticated_year = ${info.domesticatedYear},
            habitat_id = (select id from habitat where val = ${info.habitat})
            where name = ${id.toString}
         """
        .update.run
        .transact[IO](transactor)
        .as(info)
  }

  def make(using database: DatabaseModule): AnimalRepository[IO] =
    new Impl(database.transactor)
}
