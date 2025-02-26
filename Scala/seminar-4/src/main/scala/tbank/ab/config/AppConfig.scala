package tbank.ab.config

import cats.effect.IO
import pureconfig.{ConfigReader, ConfigSource}
import pureconfig.ConfigConvert.catchReadError
import pureconfig.configurable.genericMapReader
import tbank.ab.domain.animal.{AnimalId, AnimalInfo}

final case class AppConfig(
  port: Int,
  auth: AuthConfig,
  animals: Map[AnimalId, AnimalInfo]
) derives ConfigReader

object AppConfig:
  def load: IO[AppConfig] =
    IO.delay(ConfigSource.default.loadOrThrow[AppConfig])
      .flatTap(conf => IO.println(conf))

  given ConfigReader[Map[AnimalId, AnimalInfo]] =
    genericMapReader[AnimalId, AnimalInfo](
      catchReadError[AnimalId](id => AnimalId(id))
    )
