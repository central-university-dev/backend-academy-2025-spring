package tbank.ab.service

import cats.MonadThrow
import cats.effect.Async
import cats.effect.std.Backpressure
import cats.syntax.all.*
import tbank.ab.domain.animal.{AnimalId, AnimalInfo}

class RateLimiterAnimalService[F[_]: MonadThrow](service: AnimalService[F], limiter: Backpressure[F])
    extends AnimalService[F] {
  def animalDescription(id: AnimalId): F[Option[String]] =
    limiter.metered(
      service.animalDescription(id)
    ).flatMap {
      case Some(res) => res.pure[F]
      case None =>
        MonadThrow[F].raiseError(new Exception("Rate limiter rejected request")) // logging, metrics, custom errors
    }

  def animalInfo(id: AnimalId): F[Option[AnimalInfo]] =
    service.animalInfo(id)

  def updateAnimalInfo(id: AnimalId, info: AnimalInfo): F[AnimalInfo] =
    service.updateAnimalInfo(id, info)

  def randomCat(): F[String] =
    service.randomCat()

}

object RateLimiterAnimalService {
  def make[F[_]: Async](service: AnimalService[F]): F[RateLimiterAnimalService[F]] =
    for {
      backpressure <- Backpressure[F](Backpressure.Strategy.Lossy, 1)
    } yield RateLimiterAnimalService(service, backpressure)
}
