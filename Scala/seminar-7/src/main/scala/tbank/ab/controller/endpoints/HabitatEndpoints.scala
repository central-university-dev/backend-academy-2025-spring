package tbank.ab.controller.endpoints

import cats.effect.IO
import sttp.capabilities.fs2.Fs2Streams
import sttp.model.headers.CookieValueWithMeta
import sttp.model.{HeaderNames, StatusCode}
import sttp.tapir.*
import tbank.ab.domain.animal.AnimalId
import tbank.ab.domain.auth.AccessToken

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
        byteArrayBody // TODO: Fix to stream
          .and(header[String](HeaderNames.ContentDisposition))
          .and(header(HeaderNames.Accept, "image/jpeg"))
      )
      .errorOut(stringBody.and(statusCode(StatusCode.NotFound)))

  val uploadAnimalImage: Endpoint[
    (Option[AccessToken], Option[CookieValueWithMeta]),
    (AnimalId, fs2.Stream[IO, Byte]),
    (String, StatusCode),
    Unit,
    Any with Fs2Streams[IO]
  ] =
    endpoint.put
      .summary("upload animal image in its habitat")
      .in("habitat" / "image")
      .securityIn(auth.bearer[Option[AccessToken]]())
      .securityIn(setCookieOpt("session"))
      .in(query[AnimalId]("animal-id"))
      .in(
        streamBinaryBody(Fs2Streams[IO])(CodecFormat.OctetStream())
      )
      .errorOut(stringBody.and(statusCode))
}
