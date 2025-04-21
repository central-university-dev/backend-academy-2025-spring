package tbank.ab.service

import cats.Monad
import cats.data.NonEmptyVector
import cats.implicits.*
import fs2.Pipe
import tbank.ab.domain.animal.AnimalId

trait ChatService[F[_]] {
  def chat(animalId: AnimalId): F[Pipe[F, String, String]]
}

object ChatService {
  def make[F[_]: Monad](using animalService: AnimalService[F]): ChatService[F] =
    new Impl(animalService)

  private val emptyChat: NonEmptyVector[String] = NonEmptyVector.of("...")

  final private class Impl[F[_]: Monad](animalService: AnimalService[F]) extends ChatService[F] {

    private def chatLogic(voice: NonEmptyVector[String]): Pipe[F, String, String] =
      _.map {
        _.split("""\s""")
          .toList
          .map(_.sum.toInt)
          .map(summ => voice.get(summ % voice.length).getOrElse(voice.head))
          .fold("")(_ + _)
      }

    override def chat(animalId: AnimalId): F[Pipe[F, String, String]] =
      animalService.animalInfo(animalId)
        .map(_.flatMap(_.voice).flatMap(NonEmptyVector.fromVector).getOrElse(emptyChat))
        .map(chatLogic)
  }
}
