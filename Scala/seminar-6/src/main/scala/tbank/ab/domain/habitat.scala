package tbank.ab.domain

import doobie.{Get, Put, Read}
import pureconfig.ConfigReader
import sttp.tapir.{Codec, Schema}
import sttp.tapir.Codec.PlainCodec
import tethys.{JsonReader, JsonWriter}

object habitat {

  enum Habitat:
    case Forest, Plains, Desert, Mountains, Ocean

  object Habitat {
    given Schema[Habitat] =
      Schema.derivedEnumeration[Habitat].defaultStringBased

    given PlainCodec[Habitat] =
      Codec.derivedEnumeration[String, Habitat].defaultStringBased

    given ConfigReader[Habitat] =
      ConfigReader[String].map(s => Habitat.valueOf(s.capitalize))

    given JsonReader[Habitat] =
      JsonReader[String].map(s => Habitat.valueOf(s.capitalize))

    given JsonWriter[Habitat] =
      JsonWriter[String].contramap(_.toString.toLowerCase)

    given Get[Habitat] = Get[String].tmap(s => Habitat.valueOf(s.capitalize))
    given Put[Habitat] = Put[String].tcontramap(_.toString.toLowerCase)
  }

  object error {
    final case class UploadError()
  }

}
