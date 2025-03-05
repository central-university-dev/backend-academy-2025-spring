package tbank.ab.wiring

import cats.effect.IO
import tbank.ab.config.AppConfig
import tbank.ab.db.DatabaseModule
import tbank.ab.repository.{AnimalRepository, AuthRepository}

final case class Repositories()(using
  val animalRepo: AnimalRepository[IO],
  val authRepo: AuthRepository[IO]
)

object Repositories:
  def make(config: AppConfig)(using DatabaseModule): IO[Repositories] =
    for
      given AnimalRepository[IO] <- IO(AnimalRepository.make)
      given AuthRepository[IO]   <- AuthRepository.make(config)
    yield Repositories()
