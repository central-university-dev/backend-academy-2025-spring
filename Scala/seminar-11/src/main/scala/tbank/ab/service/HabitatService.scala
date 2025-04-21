package tbank.ab.service

import cats.effect.Async
import cats.implicits.*
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
  def uploadImage(animalId: AnimalId, image: Stream[F, Byte]): F[Either[UploadError, Unit]]
}

object HabitatService {
  def make[F[_]: Async](using s3: S3[F], config: S3Config): HabitatService[F] =
    new Impl(s3, config)

  final private class Impl[F[_]: Async](
    s3Client: S3[F],
    config: S3Config
  ) extends HabitatService[F] {

    override def findImage(animalId: AnimalId): F[Option[Array[Byte]]] = // TODO: Fix to stream
      NonEmptyString.from(s"${animalId.toString}.jpg") match {
        case Right(key) =>
          s3Client.readFile(config.bucket, FileKey(key))
            .compile.toVector.map(_.toArray) // TODO: Fix toArray
            .attempt.map(_.toOption)         // TODO: Make error handling
        case Left(key) => None.pure[F]
      }

    override def uploadImage(
      animalId: AnimalId,
      image: Stream[F, Byte]
    ): F[Either[UploadError, Unit]] =
      NonEmptyString.from(s"${animalId.toString}.jpg") match {
        case Right(key) => image.through(s3Client.uploadFile(config.bucket, FileKey(key))).compile.drain.map(Right(_))
        case Left(_)    => Left(UploadError()).pure[F]
      }

  }

}
