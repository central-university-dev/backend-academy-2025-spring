package tbank.ab.service

import cats.{Monad, MonadThrow}
import cats.implicits.*
import fs2.{Chunk, Stream}
import tbank.ab.domain.RequestContext
import tbank.ab.domain.animal.{AnimalId, AnimalInfo}
import tbank.ab.repository.AnimalRepository
import tofu.logging.LoggingCompanion
import tofu.syntax.logging.*

import java.nio.charset.StandardCharsets

trait AnimalService[F[_]] {
  def animalDescription(id: AnimalId): F[Option[String]]
  def animalInfo(id: AnimalId): F[Option[AnimalInfo]]
  def updateAnimalInfo(id: AnimalId, info: AnimalInfo): F[AnimalInfo]
  def randomCat(): F[String]
}

object AnimalService {

  def make[F[_]: MonadThrow](using
    repo: AnimalRepository[F],
    randomCatService: RandomCatService[F]
  ): AnimalService[F] =
    Impl(repo, randomCatService)

  final private class Impl[F[_]: Monad](repo: AnimalRepository[F], randomCatService: RandomCatService[F])
    extends AnimalService[F] {

    override def animalDescription(id: AnimalId): F[Option[String]] =
      repo.find(id)
        .map(_.map(_.description)) 

    override def animalInfo(id: AnimalId): F[Option[AnimalInfo]] =
      repo.find(id)

    override def updateAnimalInfo(
      id: AnimalId,
      info: AnimalInfo
    ): F[AnimalInfo] =
      repo.update(id, info)

    override def randomCat(): F[String] = randomCatService.randomCatFact()
  }

  private def strToStream[F[_]](str: String): Stream[F, Byte] =
    Stream.chunk(Chunk.from(str.getBytes(StandardCharsets.UTF_8)))

}
