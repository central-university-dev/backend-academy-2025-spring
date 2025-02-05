package tbank.ab.service

import cats.effect.IO
import tbank.ab.domain.animal.AnimalId

import java.nio.file.{Files, Paths}

trait HabitatService[F[_]]:
  def findImage(animalId: AnimalId): F[Option[Array[Byte]]]

object HabitatService:
  final private class Impl extends HabitatService[IO]:
    override def findImage(animalId: AnimalId): IO[Option[Array[Byte]]] =
      IO.blocking {
        val resource = this.getClass.getResource(s"/images/$animalId.jpg")

        for
          resource <- Option(resource.toURI)
          path = Paths.get(resource)
          file <-
            if Files.exists(path) then Some(Files.readAllBytes(path))
            else Option.empty
        yield file
      }.handleError(_ => Option.empty)

  def make: HabitatService[IO] = Impl()
