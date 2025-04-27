package tbank.ab.wiring

import cats.effect.Sync
import sttp.capabilities.WebSockets
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.server.ServerEndpoint
import tbank.ab.config.AppConfig
import tbank.ab.controller.{AnimalController, AuthController, Controller}

final class PublicApi[F[_]](
  authController: Controller[F],
  animalController: Controller[F]
) extends Controller[F] {

  override def endpoints: List[ServerEndpoint[Any, F]] =
    authController.endpoints ++
    animalController.endpoints
}

object PublicApi {

  def make[I[_]: Sync, F[_]: Sync](using services: Services[I, F], config: AppConfig): PublicApi[F] = {
    import config.given
    import services.given

    val authController: Controller[F]   = AuthController.make
    val animalController: Controller[F] = AnimalController.make

    PublicApi(authController, animalController)
  }

}
