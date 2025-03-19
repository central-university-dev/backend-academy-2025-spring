package tbank.ab.wiring

import cats.effect.IO
import sttp.capabilities.WebSockets
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.server.ServerEndpoint
import tbank.ab.config.AppConfig
import tbank.ab.controller.{AnimalController, AuthController, ChatController, Controller, HabitatController}

final class PublicApi(
  authController: Controller[IO],
  animalController: Controller[IO],
  habitatController: Controller[IO],
  chatController: Controller[IO]
) extends Controller[IO] {

  override def endpoints: List[ServerEndpoint[Fs2Streams[IO] & WebSockets, IO]] =
    authController.endpoints ++
    animalController.endpoints ++
    habitatController.endpoints ++
    chatController.endpoints
}

object PublicApi {

  def make(using services: Services, config: AppConfig): PublicApi = {
    import config.given
    import services.given

    val authController: Controller[IO]    = AuthController.make
    val animalController: Controller[IO]  = AnimalController.make
    val habitatController: Controller[IO] = HabitatController.make
    val chatController: Controller[IO]    = ChatController.make

    PublicApi(authController, animalController, habitatController, chatController)
  }

}
