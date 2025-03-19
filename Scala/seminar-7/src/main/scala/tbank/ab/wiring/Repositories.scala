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
  def make(using db: DatabaseModule, config: AppConfig, clients: Clients): IO[Repositories] = {
    import clients.given
    import config.given

    for {
      given AnimalRepository[IO] <- IO(AnimalRepository.make)
      given AuthRepository[IO] = AuthRepository.make
    } yield Repositories()
  }
