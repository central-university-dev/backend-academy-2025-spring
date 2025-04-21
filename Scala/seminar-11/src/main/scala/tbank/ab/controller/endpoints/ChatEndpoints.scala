package tbank.ab.controller.endpoints

import fs2.Pipe
import sttp.capabilities
import sttp.capabilities.WebSockets
import sttp.capabilities.fs2.Fs2Streams
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.json.tethysjson.jsonBody
import sttp.tapir.model.UsernamePassword
import tbank.ab.domain.animal.{AnimalId, AnimalInfo}

object ChatEndpoints:

  def animalChat[F[_]] =
    endpoint
      .summary("Chat with animal")
      .in("animal" / path[AnimalId]("animal-id") / "chat")
      .out(webSocketBody[String, CodecFormat.TextPlain, String, CodecFormat.TextPlain](Fs2Streams[F]))
