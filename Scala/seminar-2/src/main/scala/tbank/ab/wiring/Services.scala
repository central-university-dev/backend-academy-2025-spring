package tbank.ab.wiring

import cats.effect.IO
import tbank.ab.config.AppConfig
import tbank.ab.service.{AnimalService, AuthService, HabitatService}

final case class Services(
  animalService: AnimalService[IO],
  authService: AuthService[IO],
  habitatService: HabitatService[IO]
)

object Services:
  def make(config: AppConfig, repos: Repositories): Services =
    val animalService  = AnimalService.make(repos.animalRepo)
    val authService    = AuthService.make(repos.authRepo, config.auth)
    val habitatService = HabitatService.make
    Services(animalService, authService, habitatService)
