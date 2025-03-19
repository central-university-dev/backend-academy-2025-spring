package tbank.ab.service

import cats.effect.IO
import cats.syntax.option.*
import eu.timepit.refined.types.string.NonEmptyString
import fs2.Stream
import fs2.aws.s3.S3
import fs2.aws.s3.models.Models.FileKey
import tbank.ab.config.S3Config
import tbank.ab.domain.animal.AnimalId
import tbank.ab.domain.habitat.error.UploadError

trait HabitatService[F[_]] {
  def findImage(animalId: AnimalId): F[Option[Array[Byte]]]
  def uploadImage(animalId: AnimalId, image: Stream[F, Byte]): IO[Either[UploadError, Unit]]
}

object HabitatService {
  def make(using s3: S3[IO], config: S3Config): HabitatService[IO] =
    new Impl(s3, config)

  final private class Impl(
    s3Client: S3[IO],
    config: S3Config
  ) extends HabitatService[IO] {

    override def findImage(animalId: AnimalId): IO[Option[Array[Byte]]] =
      NonEmptyString.from(s"${animalId.toString}.jpg") match {
        case Right(key) =>
          s3Client.readFile(config.bucket, FileKey(key))
            .compile.toVector.map(_.toArray)
            .attempt.map(_.toOption)
        case Left(key) => IO.none
      }

    override def uploadImage(
      animalId: AnimalId,
      image: Stream[IO, Byte]
    ): IO[Either[UploadError, Unit]] =
      NonEmptyString.from(s"${animalId.toString}.jpg") match {
        case Right(key) => image.through(s3Client.uploadFile(config.bucket, FileKey(key))).compile.drain.map(Right(_))
        case Left(_)    => IO.pure(Left(UploadError()))
      }

  }

}
