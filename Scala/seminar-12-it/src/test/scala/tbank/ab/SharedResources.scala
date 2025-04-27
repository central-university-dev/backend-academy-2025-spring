/*
package tbank.ab

import cats.effect.{IO, Resource}
import com.dimafeng.testcontainers.MockServerContainer
import org.mockserver
import org.mockserver.client.MockServerClient
import sttp.client3.{SttpBackend, UriContext}
import sttp.client3.httpclient.cats.HttpClientCatsBackend
import sttp.model.Uri
import weaver.*

case class MyResources(
  appHost: Uri,
  testClient: SttpBackend[IO, Any],
  mockClient: MockServerClient,
  mockHost: Uri
)

object SharedResources extends GlobalResource {

  override def sharedResources(global: GlobalWrite): Resource[IO, Unit] =
    for {
      // start mock server
      mockServer <- mockServer
      mockClient <- mockClient(mockServer).toResource
      mockHost = uri"http://${mockServer.serverHost}:${mockServer.serverPort}"

      // start application
      server <- Seminar12App.application
      client <- HttpClientCatsBackend.resource[IO]()
      appHost = uri"localhost:8080"

      resource = MyResources(appHost, client, mockClient, mockHost)

      _ <- global.putR(value = resource)
    } yield ()

  private def mockServer: Resource[IO, MockServerContainer] =
    Resource.make(IO {
      val container = MockServerContainer(mockserver.version.Version.getVersion)
      container.start()
      container
    })(container => IO(container.stop()))

  private def mockClient(mockServer: MockServerContainer): IO[MockServerClient] =
    IO(new MockServerClient(mockServer.serverHost, mockServer.serverPort))

}
*/
