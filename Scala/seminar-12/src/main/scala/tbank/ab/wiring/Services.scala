package tbank.ab.wiring

import cats.effect.Async
import cats.syntax.all.*
import tbank.ab.config.AppConfig
import tbank.ab.domain.{RequestContext, RequestIO}
import tbank.ab.service.{AnimalService, AuthService, LoggingAnimalService, RandomCatService, RateLimiterAnimalService}
import tofu.logging.Logging

final case class Services[F[_]]()(using
  val animalService: AnimalService[F],
  val authService: AuthService[F],
  val randomCatService: RandomCatService[F]
)

object Services:
  def make[F[_]: Async](using
    config: AppConfig,
    repos: Repositories[F],
    clients: Clients[F],
    logging: Logging.Make[F]
  ): F[Services[F]] = {
    import clients.given
    import config.given
    import repos.given

    given AuthService[F]      = AuthService.make[F]
    given RandomCatService[F] = RandomCatService.make[F]

    val animalService: AnimalService[F] = AnimalService.make[F]

    RateLimiterAnimalService.make[F](animalService).map { service =>
      given AnimalService[F] = LoggingAnimalService.make(service)

      Services()
    }
  }
