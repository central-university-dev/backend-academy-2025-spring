package tbank.ab.wiring

import cats.effect.Sync
import sttp.capabilities.WebSockets
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.server.ServerEndpoint
import tbank.ab.config.AppConfig
import tbank.ab.controller.{AnimalController, AuthController, ChatController, Controller, HabitatController}

final class PublicApi[F[_]](
  authController: Controller[F],
  animalController: Controller[F],
  habitatController: Controller[F],
  chatController: Controller[F]
) extends Controller[F] {

  override def endpoints: List[ServerEndpoint[Fs2Streams[F] & WebSockets, F]] =
    authController.endpoints ++
    animalController.endpoints ++
    habitatController.endpoints ++
    chatController.endpoints
}

object PublicApi {

  def make[I[_]: Sync, F[_]: Sync](using services: Services[I, F], config: AppConfig): PublicApi[F] = {
    import config.given
    import services.given

    val authController: Controller[F]    = AuthController.make
    val animalController: Controller[F]  = AnimalController.make
    val habitatController: Controller[F] = HabitatController.make
    val chatController: Controller[F]    = ChatController.make

    PublicApi(authController, animalController, habitatController, chatController)
  }

}
