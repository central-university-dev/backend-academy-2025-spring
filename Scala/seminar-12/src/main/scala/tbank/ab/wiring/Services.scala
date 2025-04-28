package tbank.ab.wiring

import cats.~>
import cats.effect.Async
import tbank.ab.config.AppConfig
import tbank.ab.domain.{RequestContext, RequestIO}
import tbank.ab.service.{AnimalService, AuthService, LoggingAnimalService, RandomCatService}
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
  ): Services[F] = {
    import clients.given
    import config.given
    import repos.given

    given AuthService[F]      = AuthService.make[F]
    given RandomCatService[F] = RandomCatService.make[F]
    given AnimalService[F]    = LoggingAnimalService.make(AnimalService.make[F])

    Services()
  }
