package tbank.ab.service

import cats.{Monad, MonadThrow, ~>}
import cats.implicits.*
import cats.tagless.ApplyK
import fs2.{Chunk, Stream}
import tbank.ab.domain.RequestContext
import tbank.ab.domain.SetupContext
import tbank.ab.domain.animal.{AnimalId, AnimalInfo}
import tbank.ab.repository.AnimalRepository
import tbank.ab.repository.AnimalRepository.given
import tofu.WithRun
import tofu.logging.LoggingCompanion
import tofu.syntax.logging.*

import java.nio.charset.StandardCharsets

trait AnimalService[F[_]] {
  def allAnimals: fs2.Stream[F, Byte]
  def animalDescription(id: AnimalId): F[Option[String]]
  def animalInfo(id: AnimalId): F[Option[AnimalInfo]]
  def updateAnimalInfo(id: AnimalId, info: AnimalInfo): F[AnimalInfo]
  def randomCat(): F[String]
}

object AnimalService extends LoggingCompanion[AnimalService]{

  def make[I[_], F[_] : MonadThrow](using repo: AnimalRepository[F], randomCatService: RandomCatService[F], logging: AnimalService.Log[F]): AnimalService[F] = {
    Impl(repo, randomCatService)
  }

  final private class Impl[I[_], F[_] : Monad](repo: AnimalRepository[F], randomCatService: RandomCatService[F])(using
    logging: AnimalService.Log[F]) extends AnimalService[F] {
    override def allAnimals: Stream[F, Byte] =
        strToStream("[") ++
        repo.getAll
          .map(_.toString)
          .map(i => s""""$i",""")
          .flatMap(strToStream)
          .dropLast ++ // TODO: Fix dropLast (byte != Char)
        strToStream("]")

    override def animalDescription(id: AnimalId): F[Option[String]] =
      repo.find(id)
        .map(_.map(_.description)) <* debugWith"Found animal description"("animalId" -> id)

    override def animalInfo(id: AnimalId): F[Option[AnimalInfo]] =
      repo.find(id) <* debugWith"Found animal info"("animalId" -> id)

    override def updateAnimalInfo(
      id: AnimalId,
      info: AnimalInfo
                                 ): F[AnimalInfo] = {
      repo.update(id, info) <* debug"NEW MESSAGE PRODUCED"
    }

    override def randomCat(): F[String] = randomCatService.randomCatFact() <* debug"Return random cat fact"
  }

  private def strToStream[F[_]](str: String): Stream[F, Byte] =
    Stream.chunk(Chunk.from(str.getBytes(StandardCharsets.UTF_8)))

}
