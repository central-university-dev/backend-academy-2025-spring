package tbank.ab.wiring

import cats.effect.IO
import tbank.ab.config.AppConfig
import tbank.ab.service.{AnimalService, AuthService, ChatService, HabitatService}

final case class Services()(using
  val animalService: AnimalService[IO],
  val authService: AuthService[IO],
  val habitatService: HabitatService[IO],
  val chatService: ChatService[IO]
)

object Services:
  def make(using config: AppConfig, repos: Repositories, clients: Clients): Services = {
    import repos.given
    import clients.given
    import config.given

    given AnimalService[IO]  = AnimalService.make
    given AuthService[IO]    = AuthService.make
    given HabitatService[IO] = HabitatService.make
    given ChatService[IO]    = ChatService.make

    Services()
  }
