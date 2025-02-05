package tbank.ab.wiring

import cats.effect.IO
import tbank.ab.config.AppConfig
import tbank.ab.repository.AnimalRepository

final case class Repositories(
  animalRepo: AnimalRepository[IO]
)

object Repositories:
  def make(config: AppConfig): IO[Repositories] =
    for animalRepo <- AnimalRepository.make(config)
    yield Repositories(animalRepo)
