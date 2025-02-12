package tbank.ab.wiring

import cats.effect.IO
import tbank.ab.config.AppConfig
import tbank.ab.repository.{AnimalRepository, AuthRepository}

final case class Repositories(
  animalRepo: AnimalRepository[IO],
  authRepo: AuthRepository[IO]
)

object Repositories:
  def make(config: AppConfig): IO[Repositories] =
    for
      animalRepo <- AnimalRepository.make(config)
      authRepo   <- AuthRepository.make(config)
    yield Repositories(animalRepo, authRepo)
