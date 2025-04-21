package tbank.ab.wiring

import cats.Monad
import sttp.capabilities.WebSockets
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.server.ServerEndpoint
import tbank.ab.controller.{Controller, ProbeController}

final class MonitoringApi[F[_]](
  probeController: Controller[F]
) extends Controller[F] {
  def endpoints: List[ServerEndpoint[Fs2Streams[F] & WebSockets, F]] =
    probeController.endpoints
}

object MonitoringApi {

  def make[F[_]: Monad]: MonitoringApi[F] = {
    val probeController: Controller[F] = ProbeController.make[F]

    MonitoringApi(probeController)
  }

}
