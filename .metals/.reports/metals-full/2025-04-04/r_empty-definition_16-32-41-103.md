error id: `<none>`.
file://<WORKSPACE>/Scala/seminar-8/src/main/scala/tbank/ab/service/AnimalService.scala
empty definition using pc, found symbol in pc: `<none>`.
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -println.
	 -println#
	 -println().
	 -scala/Predef.println.
	 -scala/Predef.println#
	 -scala/Predef.println().
offset: 1470
uri: file://<WORKSPACE>/Scala/seminar-8/src/main/scala/tbank/ab/service/AnimalService.scala
text:
```scala
package tbank.ab.service

import cats.effect.IO
import fs2.{Chunk, Stream}
import tbank.ab.domain.animal.{AnimalId, AnimalInfo}
import tbank.ab.repository.AnimalRepository

import java.nio.charset.StandardCharsets
import tbank.ab.mq.AnimalUpdateProducer

trait AnimalService[F[_]] {
  def allAnimals: fs2.Stream[F, Byte]
  def animalDescription(id: AnimalId): F[Option[String]]
  def animalInfo(id: AnimalId): F[Option[AnimalInfo]]
  def updateAnimalInfo(id: AnimalId, info: AnimalInfo): F[AnimalInfo]
}

object AnimalService {

  def make(using repo: AnimalRepository[IO], producer: AnimalUpdateProducer[IO]): AnimalService[IO] = 
    Impl(repo, producer)

  final private class Impl(repo: AnimalRepository[IO], producer: AnimalUpdateProducer[IO]) extends AnimalService[IO] {
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
    ): IO[AnimalInfo] = {
      println("NEW MESSAGE SERVICE INSIDE")
      repo.update(id, info) <* producer.produce(id) <* IO.pure(print@@ln("NEW MESSAGE PRODUCED"))
    }

    override def saveAnimalInfo(id: AnimalId, info: AnimalInfo): IO[Unit] = 
      repo.save(id, info)
  }

  private def strToStream(str: String): Stream[IO, Byte] =
    Stream.chunk(Chunk.from(str.getBytes(StandardCharsets.UTF_8)))

}

```


#### Short summary: 

empty definition using pc, found symbol in pc: `<none>`.