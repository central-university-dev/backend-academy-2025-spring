package tbank.ab.service

import cats.effect.IO
import fs2.Pipe
import tbank.ab.domain.animal.AnimalId

trait ChatService[F[_]] {
  def chat(animalId: AnimalId): F[Pipe[F, String, String]]
}

object ChatService {
  def make(using animalService: AnimalService[IO]): ChatService[IO] =
    new Impl(animalService)
  
  final private class Impl(animalService: AnimalService[IO]) extends ChatService[IO] {

    override def chat(animalId: AnimalId): IO[Pipe[IO, String, String]] = ???
  }
}
