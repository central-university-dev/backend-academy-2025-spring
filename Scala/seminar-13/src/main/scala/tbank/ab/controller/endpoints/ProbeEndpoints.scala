package tbank.ab.controller.endpoints

import sttp.tapir.*

object ProbeEndpoints:
  val live: Endpoint[Unit, Unit, Unit, Unit, Any] =
    endpoint.get
      .summary("live probe")
      .in("live")
