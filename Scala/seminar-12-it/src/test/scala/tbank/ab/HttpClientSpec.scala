package tbank.ab

import cats.effect.{IO, Resource}
import com.dimafeng.testcontainers.WireMockContainer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import org.http4s.client.Client
import org.http4s.{Request, Uri}
import tbank.ab.wiring.Clients
import weaver.*

import java.util.concurrent.TimeoutException

case class HttpClientResources(
                                testClient: Client[IO],
                                wireMock: WireMock,
                                publicUri: Uri
)

object HttpClientSpec extends IOSuite {

  override type Res = HttpClientResources

  override def sharedResource: Resource[IO, HttpClientResources] = {
    for {
      container <- wireMockContainer
      wireMockClient = new WireMock(container.getHost, container.getPort)
      client <- Clients.httpClientResource[IO]
      uri = Uri.unsafeFromString(s"http://${container.getHost}:${container.getPort}")
    } yield HttpClientResources(client, wireMockClient, uri)
  }

  private def wireMockContainer: Resource[IO, WireMockContainer] =
    Resource.make(IO {
      val container = WireMockContainer.Def()
      container.start()
    })(container => IO(container.stop()))


  test("HttpClient should handle GET request") { (resource: HttpClientResources) =>
    val testPath = "/test"

    val request = Request[IO](uri = Uri.unsafeFromString(s"${resource.publicUri}$testPath"))
    for {
     _ <- IO.delay(
         resource.wireMock.register(
            get(urlEqualTo(testPath))
              .willReturn(
                aResponse()
                  .withBody("wire mock response")
                  .withStatus(200)
              )
          )
       )
     response <- resource.testClient.expect[String](request)
    } yield expect(response == "wire mock response")
  }

  test("HttpClient should handle timeout") { (resource: HttpClientResources) =>
    val testPath = "/timeout"

    val request = Request[IO](uri = Uri.unsafeFromString(s"${resource.publicUri}$testPath"))
    for {
      _ <- IO.delay(
        resource.wireMock.register(
          get(urlEqualTo(testPath))
            .willReturn(
              aResponse()
                .withBody("wire mock response")
                .withFixedDelay(1500)
                .withStatus(200)
            )
        )
      )
      response <- resource.testClient.expect[String](request).attempt
    } yield
      response match
        case Left(_ : TimeoutException) => success
        case _ => failure("expected TimeoutException")
  }
}
