package tbank.ab.service

import cats.effect.IO
import fs2.Stream
import fs2.io.file.{Files, Path}
import tbank.ab.domain.animal.AnimalId
import tbank.ab.domain.habitat.error.UploadError

import java.nio.file.{Files as NioFiles, Paths as NioPaths}

trait HabitatService[F[_]]:
  def findImage(animalId: AnimalId): F[Option[Array[Byte]]]
  def uploadImage(
    animalId: AnimalId,
    image: Stream[F, Byte]
  ): IO[Either[UploadError, Unit]]

object HabitatService:
  final private class Impl extends HabitatService[IO]:

    private val imagesDir: String = "seminar-2/images"

    override def findImage(animalId: AnimalId): IO[Option[Array[Byte]]] =
      IO.blocking {
        val path = NioPaths.get(s"$imagesDir/$animalId.jpg")
        val file =
          if NioFiles.exists(path) then Some(NioFiles.readAllBytes(path))
          else Option.empty
        file
      }.handleError(_ => Option.empty)

    override def uploadImage(
      animalId: AnimalId,
      image: Stream[IO, Byte]
    ): IO[Either[UploadError, Unit]] =
      val nioPath = NioPaths.get(s"$imagesDir/$animalId.jpg")
      val path    = Path.fromNioPath(nioPath)
      image.through(Files[IO].writeAll(path))
        .compile
        .drain
        .handleError(_ => Left(UploadError()))
        .map(_ => Right(()))

  def make: HabitatService[IO] = Impl()
