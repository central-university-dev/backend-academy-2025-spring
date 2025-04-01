package tbank.ab.service

import cats.effect.IO
import tbank.ab.domain.animal.{AnimalId, AnimalInfo}
import tbank.ab.repository.AnimalRepository

trait AnimalService[F[_]]:
  def animalDescription(id: AnimalId): F[Option[String]]
  def animalInfo(id: AnimalId): F[Option[AnimalInfo]]
  def updateAnimalInfo(id: AnimalId, info: AnimalInfo): F[AnimalInfo]

object AnimalService:
  final private class Impl(repo: AnimalRepository[IO]) extends AnimalService[IO]:
    override def animalDescription(id: AnimalId): IO[Option[String]] =
      for info <- repo.find(id)
      yield info.map(_.description)

    override def animalInfo(id: AnimalId): IO[Option[AnimalInfo]] =
      for info <- repo.find(id)
      yield info

    override def updateAnimalInfo(
      id: AnimalId,
      info: AnimalInfo
    ): IO[AnimalInfo] =
      for info <- repo.update(id, info)
      yield info

  def make(repo: AnimalRepository[IO]): AnimalService[IO] =
    Impl(repo)
