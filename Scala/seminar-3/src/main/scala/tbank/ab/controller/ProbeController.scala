package tbank.ab.controller

import cats.effect.IO
import sttp.tapir.*
import sttp.tapir.server.ServerEndpoint
import tbank.ab.controller.endpoints.ProbeEndpoints

private class ProbeController extends Controller[IO]:

  private val live: ServerEndpoint[Any, IO] =
    ProbeEndpoints.live
      .serverLogicSuccess(IO.pure)

  override val endpoints: List[ServerEndpoint[Any, IO]] =
    List(live)
      .map(_.withTag("Probes"))

object ProbeController:
  def make: Controller[IO] = new ProbeController()
