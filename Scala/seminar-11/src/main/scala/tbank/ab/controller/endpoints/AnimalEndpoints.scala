package tbank.ab.controller.endpoints

import cats.effect.IO
import sttp.capabilities
import sttp.capabilities.fs2.Fs2Streams
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.json.tethysjson.jsonBody
import sttp.tapir.model.UsernamePassword
import tbank.ab.domain.animal.{AnimalId, AnimalInfo}

object AnimalEndpoints {
  val allAnimals: Endpoint[Unit, Unit, Unit, fs2.Stream[IO, Byte], Fs2Streams[IO]] =
    endpoint.get
      .summary("returns all animals ids")
      .in("animal")
      .out(
        streamBody[Fs2Streams[IO], List[AnimalId]](Fs2Streams[IO])(
          Schema.derived[List[AnimalId]],
          CodecFormat.Json(),
          None
        )
      )

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


  val randomFact: Endpoint[Unit, Unit, String, String, Any] =
    endpoint.get
      .summary("random cat fact")
      .in("animal" / "fact" / "random")
      .out(stringBody)
      .errorOut(stringBody.and(statusCode(StatusCode.ServiceUnavailable)))

}
