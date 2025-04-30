package tbank.ab.domain

import org.typelevel.otel4s.Attribute
import sttp.tapir.{Codec, Schema}
import tbank.ab.domain.habitat.Habitat
import tethys.{JsonReader, JsonWriter}
import tofu.logging.Loggable

object animal {

  opaque type AnimalId = String
  object AnimalId:
    def apply(id: String): AnimalId = id

    given (using c: Codec.PlainCodec[String]): Codec.PlainCodec[AnimalId] = c
    given (using s: Schema[String]): Schema[AnimalId]                     = s
    given (using jr: JsonReader[String]): JsonReader[AnimalId]            = jr
    given (using jw: JsonWriter[String]): JsonWriter[AnimalId]            = jw
    given (using log: Loggable[String]): Loggable[AnimalId]               = log
    given Attribute.From[AnimalId, String]                                = _.toString
    given Attribute.Make[AnimalId, String]                                = Attribute.Make.const("animal.id")

  final case class AnimalInfo(
    description: String,
    habitat: Habitat,
    features: List[String],
    domesticatedYear: Option[Int],
    voice: Option[Vector[String]]
  ) derives Schema,
        JsonReader,
        JsonWriter
}
