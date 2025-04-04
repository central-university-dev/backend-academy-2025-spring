package tbank.ab.service

import cats.data.NonEmptyVector
import cats.effect.IO
import fs2.Pipe
import tbank.ab.domain.animal.AnimalId

trait ChatService[F[_]] {
  def chat(animalId: AnimalId): F[Pipe[F, String, String]]
}

object ChatService {
  def make(using animalService: AnimalService[IO]): ChatService[IO] =
    new Impl(animalService)

  private val emptyChat: NonEmptyVector[String] = NonEmptyVector.of("...")

  final private class Impl(animalService: AnimalService[IO]) extends ChatService[IO] {

    private def chatLogic(voice: NonEmptyVector[String]): Pipe[IO, String, String] =
      _.map {
        _.split("""\s""")
          .toList
          .map(_.sum.toInt)
          .map(summ => voice.get(summ % voice.length).getOrElse(voice.head))
          .fold("")(_ + _)
      }

    override def chat(animalId: AnimalId): IO[Pipe[IO, String, String]] =
      animalService.animalInfo(animalId)
        .map(_.flatMap(_.voice).flatMap(NonEmptyVector.fromVector).getOrElse(emptyChat))
        .map(chatLogic)
  }
}
