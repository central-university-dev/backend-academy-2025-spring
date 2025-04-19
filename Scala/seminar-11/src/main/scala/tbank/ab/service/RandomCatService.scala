package tbank.ab.service

import cats.effect.IO
import org.http4s.client.Client
import tbank.ab.config.RandomCatServiceConfig
import tofu.logging.LoggingCompanion
import tofu.syntax.logging.*

trait RandomCatService[F[_]] {
    def randomCatFact(): F[String]
}

object RandomCatService extends LoggingCompanion[RandomCatService]{
    def make(using 
            httpClient: Client[IO],
            config: RandomCatServiceConfig,
            logging: RandomCatService.Log[IO]
            ): RandomCatService[IO] = new RandomCatService[IO] {
        def randomCatFact() =
          debug"Searching for random fact..." *> httpClient.expect[String](config.randomCatFactUri)
    }
}
