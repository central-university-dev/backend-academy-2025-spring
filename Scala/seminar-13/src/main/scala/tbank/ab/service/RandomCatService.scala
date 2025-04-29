package tbank.ab.service

import cats.effect.Async
import cats.implicits.*
import org.http4s.EntityDecoder
import org.http4s.client.Client
import tbank.ab.config.RandomCatServiceConfig
import tofu.logging.LoggingCompanion
import tofu.syntax.logging.*

trait RandomCatService[F[_]] {
  def randomCatFact(): F[String]
}

object RandomCatService extends LoggingCompanion[RandomCatService] {
  def make[F[_]: Async](using
    httpClient: Client[F],
    config: RandomCatServiceConfig,
    logging: RandomCatService.Log[F]
  ): RandomCatService[F] =
    new Impl[F](httpClient, config)

  private class Impl[F[_]: Async](httpClient: Client[F], config: RandomCatServiceConfig)(using
    logging: RandomCatService.Log[F]
  ) extends RandomCatService[F] {
    def randomCatFact() =
      debug"Searching for random fact..." *>
      httpClient.expect[String](config.randomCatFactUri).onError(e => errorCause"Failed to find random fact" (e.getCause))

    given EntityDecoder[F, String] = EntityDecoder.text[F]
  }

}
