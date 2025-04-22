package tbank.ab.wiring

import cats.~>
import cats.effect.Async
import tbank.ab.config.AppConfig
import tbank.ab.domain.{RequestContext, RequestIO}
import tbank.ab.service.{AnimalService, AuthService, ChatService, HabitatService, RandomCatService}
import tofu.logging.Logging

final case class Services[I[_], F[_]]()(using
  val animalService: AnimalService[F],
  val authService: AuthService[F],
  val habitatService: HabitatService[F],
  val chatService: ChatService[F],
  val randomCatService: RandomCatService[F]
)

object Services:
  def make[I[_], F[_]: Async](using
    config: AppConfig,
    repos: Repositories[F],
    clients: Clients[F],
    logging: Logging.Make[F]
  ): Services[I, F] = {
    import clients.given
    import config.given
    import repos.given

    given AuthService[F]      = AuthService.make[F]
    given HabitatService[F]   = HabitatService.make[F]
    given RandomCatService[F] = RandomCatService.make[F]
    given AnimalService[F]    = AnimalService.make[I, F]
    given ChatService[F]      = ChatService.make[F]

    Services()
  }
