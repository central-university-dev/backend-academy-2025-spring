package tbank.ab.controller.endpoints

import sttp.capabilities
import sttp.capabilities.fs2.Fs2Streams
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.json.tethysjson.jsonBody
import sttp.tapir.model.UsernamePassword
import tbank.ab.domain.animal.{AnimalId, AnimalInfo}

object AnimalEndpoints {
  def allAnimals[F[_]]: Endpoint[Unit, Unit, Unit, fs2.Stream[F, Byte], Fs2Streams[F]] =
    endpoint.get
      .summary("returns all animals ids")
      .in("animal")
      .out(
        streamBody[Fs2Streams[F], List[AnimalId]](Fs2Streams[F])(
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
