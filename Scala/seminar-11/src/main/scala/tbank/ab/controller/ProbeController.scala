package tbank.ab.controller

import cats.Monad
import sttp.tapir.*
import sttp.tapir.server.ServerEndpoint
import tbank.ab.controller.endpoints.ProbeEndpoints

private class ProbeController[F[_]: Monad] extends Controller[F] {

  private val live: ServerEndpoint[Any, F] =
    ProbeEndpoints.live
      .serverLogicSuccess(Monad[F].pure)

  override val endpoints: List[ServerEndpoint[Any, F]] =
    List(live)
      .map(_.withTag("Probes"))
}

object ProbeController {
  def make[F[_]: Monad]: Controller[F] = new ProbeController[F]()
}
