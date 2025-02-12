package tbank.ab.controller.endpoints

import sttp.model.{HeaderNames, StatusCode}
import sttp.tapir.*
import tbank.ab.domain.animal.AnimalId

object HabitatEndpoints {
  val animalImage: Endpoint[
    Unit,
    AnimalId,
    String,
    (Array[Byte], String),
    Any
  ] =
    endpoint.get
      .summary("animal image in its habitat")
      .in("habitat" / "image")
      .in(query[AnimalId]("animal-id"))
      .out(
        byteArrayBody
          .and(header[String](HeaderNames.ContentDisposition))
          .and(header(HeaderNames.Accept, "image/jpeg"))
      )
      .errorOut(stringBody.and(statusCode(StatusCode.NotFound)))

  val uploadAnimalImage = ???
}
