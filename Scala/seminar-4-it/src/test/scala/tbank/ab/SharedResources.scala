package tbank.ab

import cats.effect.{IO, Resource}
import org.http4s.server.Server
import sttp.client3.SttpBackend
import sttp.client3.httpclient.cats.HttpClientCatsBackend
import weaver.*

case class MyResources(server: Server, backend: SttpBackend[IO, Any])

object SharedResources extends GlobalResource {
  def sharedResources(global: GlobalWrite): Resource[IO, Unit] =
    for {
      server: Server               <- Seminar4App.application
      client: SttpBackend[IO, Any] <- HttpClientCatsBackend.resource[IO]()
      resource = MyResources(server, client)
      _ <- global.putR(value = resource, label = Some("my-resource"))
    } yield ()
}
