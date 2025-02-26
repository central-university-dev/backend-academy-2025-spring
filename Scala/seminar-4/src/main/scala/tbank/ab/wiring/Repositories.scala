package tbank.ab.wiring

import cats.effect.IO
import tbank.ab.config.AppConfig
import tbank.ab.repository.{AnimalRepository, AuthRepository}

final case class Repositories()(using
  val animalRepo: AnimalRepository[IO],
  val authRepo: AuthRepository[IO]
)

object Repositories:
  def make(config: AppConfig): IO[Repositories] =
    for
      given AnimalRepository[IO] <- AnimalRepository.make(config)
      given AuthRepository[IO]   <- AuthRepository.make(config)
    yield Repositories()
