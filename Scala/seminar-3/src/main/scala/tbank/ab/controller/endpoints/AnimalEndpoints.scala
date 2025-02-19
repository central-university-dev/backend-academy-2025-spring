package tbank.ab.controller.endpoints

import cats.effect.IO
import fs2.Pipe
import sttp.capabilities
import sttp.capabilities.WebSockets
import sttp.capabilities.fs2.Fs2Streams
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.json.tethysjson.jsonBody
import sttp.tapir.model.UsernamePassword
import tbank.ab.domain.animal.{AnimalId, AnimalInfo}

object AnimalEndpoints:
  val animalDescription: Endpoint[Unit, AnimalId, String, String, Any] =
    endpoint.get
      .summary("animal description")
      .in("animal" / path[AnimalId]("animal-id") / "description")
      .out(stringBody)
      .errorOut(stringBody.and(statusCode(StatusCode.NotFound)))

  val animalInfo: Endpoint[Unit, AnimalId, String, AnimalInfo, Any] =
    endpoint.get
      .summary("animal info")
      .in("animal" / path[AnimalId]("animal-id"))
      .out(jsonBody[AnimalInfo])
      .errorOut(stringBody.and(statusCode(StatusCode.NotFound)))

  val updateAnimalInfoEndpoint: Endpoint[
    UsernamePassword,
    (AnimalId, AnimalInfo),
    String,
    AnimalInfo,
    Any
  ] =
    endpoint.put
      .summary("update animal info")
      .in("animal" / path[AnimalId]("animal-id"))
      .in(jsonBody[AnimalInfo])
      .securityIn(auth.basic[UsernamePassword]())
      .out(jsonBody[AnimalInfo])
      .errorOut(stringBody.and(statusCode(StatusCode.Unauthorized)))

  val animalChat =
    endpoint.get
      .summary("animal info")
      .in("animal" / path[AnimalId]("animal-id") / "chat")
      .out(webSocketBody[Option[String], CodecFormat.TextPlain, Option[String], CodecFormat.TextPlain](Fs2Streams[IO]))
