package tbank.ab.service

import cats.Monad
import cats.syntax.all.*
import tbank.ab.domain.animal.{AnimalId, AnimalInfo}
import tofu.logging.LoggingCompanion
import tofu.syntax.location.logging.LoggingInterpolator

class LoggingAnimalService[F[_]: Monad](service: AnimalService[F])(using logging: LoggingAnimalService.Log[F])
    extends AnimalService[F] {
  def animalDescription(id: AnimalId): F[Option[String]] =
    service.animalDescription(id) <* debugWith"Found animal description" ("animalId" -> id)

  def animalInfo(id: AnimalId): F[Option[AnimalInfo]] =
    service.animalInfo(id) <* debugWith"Found animal info" ("animalId" -> id)

  def updateAnimalInfo(id: AnimalId, info: AnimalInfo): F[AnimalInfo] =
    service.updateAnimalInfo(id, info) <* debugWith"Updated animal info" ("animalId" -> id)

  def randomCat(): F[String] =
    service.randomCat() <* debug"Return random cat fact"

}

object LoggingAnimalService extends LoggingCompanion[LoggingAnimalService] {
  def make[F[_]: Monad](service: AnimalService[F])(using
    loggingAnimalService: LoggingAnimalService.Log[F]
  ): AnimalService[F] =
    new LoggingAnimalService(service)
}
