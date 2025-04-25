package tbank.ab.repository

import cats.effect.*
import cats.implicits.*
import doobie.*
import doobie.implicits.*
import fs2.Stream
import tbank.ab.db.DatabaseModule
import tbank.ab.domain.animal.{AnimalId, AnimalInfo}
import tbank.ab.domain.habitat.Habitat
import tbank.ab.repository.model.AnimalInfoRepository

trait AnimalRepository[F[_]] {
  def getAll: Stream[F, AnimalId]
  def find(id: AnimalId): F[Option[AnimalInfo]]
  def update(id: AnimalId, info: AnimalInfo): F[AnimalInfo]
}

object AnimalRepository {

  private class Impl[F[_]: Async](
    transactor: Transactor[F]
  ) extends AnimalRepository[F] {
    override def getAll: Stream[F, AnimalId] =
      sql"select name from animal_info"
        .query[String]
        .map(AnimalId.apply)
        .stream
        .transact(transactor)

    override def find(id: AnimalId): F[Option[AnimalInfo]] =
      (
        for {
          animalId <- sql"select id from animal_info where name = ${id.toString}".query[Long].unique
          voices   <- sql"""select voice from voices where animal_id = $animalId""".query[String].to[Vector]
          animalInfo <- sql"""select a.id, name, description, val, domesticated_year from animal_info as a
           left join habitat as h on habitat_id = h.id where name = ${id.toString}
           """.query[AnimalInfoRepository].option
        } yield animalInfo.map(info =>
          AnimalInfo(
            description = info.description,
            habitat = info.habitat,
            features = List.empty,
            domesticatedYear = info.domesticatedYear,
            voice = Some(voices)
          )
        )
      ).transact[F](transactor)

    override def update(id: AnimalId, info: AnimalInfo): F[AnimalInfo] = (
      for {
        // TODO: Move query and update to object RepositoryQuery (and reuse)
        animalId <- sql"select id from animal_info where name = ${id.toString}".query[Long].unique
        _        <- sql"""delete from voices where animal_id = $animalId""".update.run
        _ <- Update[String](s"insert into voices(animal_id, voice) values ($animalId, ?)")
               .updateMany(info.voice.getOrElse(Vector.empty))
        _ <- sql"""update animal_info set
            description = ${info.description},
            domesticated_year = ${info.domesticatedYear},
            habitat_id = (select id from habitat where val = ${info.habitat})
            where name = ${id.toString}
         """.update.run
      } yield ()
    )
      .transact[F](transactor)
      .as(info)
  }

  def make[F[_]: Async](using database: DatabaseModule[F]): AnimalRepository[F] =
    new Impl(database.transactor)
}
