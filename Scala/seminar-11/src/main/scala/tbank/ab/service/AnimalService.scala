package tbank.ab.service

import cats.effect.IO
import fs2.{Chunk, Stream}
import tbank.ab.domain.animal.{AnimalId, AnimalInfo}
import tbank.ab.repository.AnimalRepository

import java.nio.charset.StandardCharsets
import tbank.ab.mq.AnimalUpdateProducer
import tofu.logging.LoggingCompanion
import tofu.syntax.logging.*

trait AnimalService[F[_]] {
  def allAnimals: fs2.Stream[F, Byte]
  def animalDescription(id: AnimalId): F[Option[String]]
  def animalInfo(id: AnimalId): F[Option[AnimalInfo]]
  def updateAnimalInfo(id: AnimalId, info: AnimalInfo): F[AnimalInfo]
  def randomCat(): F[String]
}

object AnimalService extends LoggingCompanion[AnimalService]{

  def make(using repo: AnimalRepository[IO], randomCatService: RandomCatService[IO], logging: AnimalService.Log[IO]): AnimalService[IO] = 
    Impl(repo, randomCatService)

  final private class Impl(repo: AnimalRepository[IO], randomCatService: RandomCatService[IO])(using logging: AnimalService.Log[IO]) extends AnimalService[IO] {
    override def allAnimals: Stream[IO, Byte] =
      strToStream("[") ++
      repo.getAll
        .map(_.toString)
        .map(i => s""""$i",""")
        .flatMap(strToStream)
        .dropLast ++ // TODO: Fix dropLast (byte != Char)
      strToStream("]")

    override def animalDescription(id: AnimalId): IO[Option[String]] =
      repo.find(id)
        .map(_.map(_.description)) <* debugWith"Found animal description"("animalId" -> id)

    override def animalInfo(id: AnimalId): IO[Option[AnimalInfo]] =
      repo.find(id) <* debugWith"Found animal info"("animalId" -> id)

    override def updateAnimalInfo(
      id: AnimalId,
      info: AnimalInfo
    ): IO[AnimalInfo] = {
      repo.update(id, info) <* debug"NEW MESSAGE PRODUCED"
    }

    override def randomCat(): IO[String] = randomCatService.randomCatFact() <* debug"Return random cat fact"
  }

  private def strToStream(str: String): Stream[IO, Byte] =
    Stream.chunk(Chunk.from(str.getBytes(StandardCharsets.UTF_8)))

}
