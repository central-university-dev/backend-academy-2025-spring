package tbank.ab.config

import cats.effect.IO
import pureconfig.{ConfigObjectSource, ConfigReader, ConfigSource}
import pureconfig.ConfigConvert.catchReadError
import pureconfig.configurable.genericMapReader
import tbank.ab.domain.animal.{AnimalId, AnimalInfo}

final case class AppConfig()(using
  val server: ServerConfig,
  val auth: AuthConfig,
  val animals: Map[AnimalId, AnimalInfo],
  val database: DbConfig
)

object AppConfig {
  private case class AppConfigView(
    server: ServerConfig,
    auth: AuthConfig,
    animals: Map[AnimalId, AnimalInfo],
    database: DbConfig
  ) derives ConfigReader

  def load(source: ConfigSource): IO[AppConfig] =
    IO.delay(source.loadOrThrow[AppConfigView])
      .map(view =>
        AppConfig()(using
          server = view.server,
          auth = view.auth,
          animals = view.animals,
          database = view.database
        )
      )

  given ConfigReader[Map[AnimalId, AnimalInfo]] =
    genericMapReader[AnimalId, AnimalInfo](
      catchReadError[AnimalId](id => AnimalId(id))
    )
}
