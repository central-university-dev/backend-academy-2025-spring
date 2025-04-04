package tbank.ab.wiring

import cats.effect.IO
import sttp.capabilities.WebSockets
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.server.ServerEndpoint
import tbank.ab.controller.{Controller, ProbeController}

final class MonitoringApi(
  probeController: Controller[IO]
) extends Controller[IO] {
  def endpoints: List[ServerEndpoint[Fs2Streams[IO] & WebSockets, IO]] =
    probeController.endpoints
}

object MonitoringApi {

  def make: MonitoringApi = {
    val probeController: Controller[IO] = ProbeController.make

    MonitoringApi(probeController)
  }

}
