package tbank.ab.service

import cats.effect.IO
import fs2.{Chunk, Stream}
import tbank.ab.domain.animal.{AnimalId, AnimalInfo}
import tbank.ab.repository.AnimalRepository

import java.nio.charset.StandardCharsets

trait AnimalService[F[_]] {
  def allAnimals: fs2.Stream[F, Byte]
  def animalDescription(id: AnimalId): F[Option[String]]
  def animalInfo(id: AnimalId): F[Option[AnimalInfo]]
  def updateAnimalInfo(id: AnimalId, info: AnimalInfo): F[AnimalInfo]
}

object AnimalService {

  def make(using repo: AnimalRepository[IO]): AnimalService[IO] =
    Impl(repo)

  final private class Impl(repo: AnimalRepository[IO]) extends AnimalService[IO] {
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
        .map(_.map(_.description))

    override def animalInfo(id: AnimalId): IO[Option[AnimalInfo]] =
      repo.find(id)

    override def updateAnimalInfo(
      id: AnimalId,
      info: AnimalInfo
    ): IO[AnimalInfo] =
      repo.update(id, info)

  }

  private def strToStream(str: String): Stream[IO, Byte] =
    Stream.chunk(Chunk.from(str.getBytes(StandardCharsets.UTF_8)))

}
