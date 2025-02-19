package tbank.ab.repository

import cats.effect.{IO, Ref}
import tbank.ab.config.AppConfig
import tbank.ab.domain.animal.{AnimalId, AnimalInfo}

trait AnimalRepository[F[_]]:
  def find(id: AnimalId): F[Option[AnimalInfo]]
  def update(id: AnimalId, info: AnimalInfo): F[AnimalInfo]

object AnimalRepository:
  final private class InMemory(
    repo: Ref[IO, Map[AnimalId, AnimalInfo]]
  ) extends AnimalRepository[IO]:
    override def find(id: AnimalId): IO[Option[AnimalInfo]] =
      for
        r <- repo.get
        info = r.get(id)
      yield info

    override def update(id: AnimalId, info: AnimalInfo): IO[AnimalInfo] =
      for r <- repo.update(map => map.updated(id, info))
      yield info

  def make(config: AppConfig): IO[AnimalRepository[IO]] =
    for ref <- Ref.of[IO, Map[AnimalId, AnimalInfo]](config.animals)
    yield InMemory(ref)
