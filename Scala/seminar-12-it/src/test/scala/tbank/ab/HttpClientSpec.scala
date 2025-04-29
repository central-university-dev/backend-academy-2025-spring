package tbank.ab

import cats.effect.{IO, Resource}
import cats.syntax.all.toTraverseOps
import com.dimafeng.testcontainers.WireMockContainer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.chrisdavenport.circuit.CircuitBreaker.RejectedExecution
import org.http4s.Method.POST
import org.http4s.client.Client
import org.http4s.headers.`Idempotency-Key`
import org.http4s.{Header, Request, Uri}
import tbank.ab.wiring.Clients
import tofu.logging.Logging
import weaver.*

import java.util.concurrent.TimeoutException
import scala.concurrent.duration.*

case class HttpClientResources(
                                testClient: Client[IO],
                                wireMock: WireMock,
                                publicUri: Uri
)

object HttpClientSpec extends IOSuite {

  given Logging.Make[IO] = Logging.Make.plain[IO]

  override type Res = HttpClientResources

  override def sharedResource: Resource[IO, HttpClientResources] =
    for {
      container <- wireMockContainer
      wireMockClient = new WireMock(container.getHost, container.getPort)
      client <- Clients.httpClientResource[IO]
      uri = Uri.unsafeFromString(s"http://${container.getHost}:${container.getPort}")
    } yield HttpClientResources(client, wireMockClient, uri)

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
                .withFixedDelay(600)
                .withStatus(200)
            )
        )
      )
      response <- resource.testClient.expect[String](request).attempt
    } yield
      response match
        case Left(_: java.util.concurrent.TimeoutException) => success
        case r => failure(s"expected TimeoutException, got $r")
  }

  test("HttpClient should retry InternalServerError") { (resource: HttpClientResources) =>
    val testPath = "/retry"

    val request = Request[IO](uri = Uri.unsafeFromString(s"${resource.publicUri}$testPath"))
    for {
      _ <- IO.delay(
        resource.wireMock.register(
          get(urlEqualTo(testPath))
            .willReturn(
              aResponse()
                .withBody("wire mock response")
                .withStatus(500)
            )
        )
      )
      response <- resource.testClient.expect[String](request).attempt
      _ <- IO(resource.wireMock.verifyThat(5, getRequestedFor(urlEqualTo(testPath))))
    } yield success
  }

  test("HttpClient should retry idempotent requests") { (resource: HttpClientResources) =>
    val testPath = "/idempotent"

    val request = Request[IO](uri = Uri.unsafeFromString(s"${resource.publicUri}$testPath"), method = POST)
      .withHeaders(`Idempotency-Key`("key"))

    for {
      _ <- IO.delay(
        resource.wireMock.register(
          post(urlEqualTo(testPath))
            .willReturn(
              aResponse()
                .withBody("wire mock response")
                .withStatus(500)
            )
        )
      )
      response <- resource.testClient.expect[String](request).attempt
      _ <- IO(resource.wireMock.verifyThat(5, postRequestedFor(urlEqualTo(testPath))))
    } yield success
  }

  test("HttpClient should open CircuitBreaker") { (resource: HttpClientResources) =>
    val testPath = "/circuit"

    val request = Request[IO](uri = Uri.unsafeFromString(s"${resource.publicUri}$testPath"))

    for {
      _ <- IO.delay(
        resource.wireMock.register(
          get(urlEqualTo(testPath))
            .willReturn(
              aResponse()
                .withBody("wire mock response")
                .withFixedDelay(600)
                .withStatus(200)
            )
        )
      )
      // State = Open
      _ <- (1 to 2).toList.traverse { _ =>
        resource.testClient.expect[String](request).attempt.map(r =>
          expect(r.swap.toOption.get.isInstanceOf[TimeoutException])
        )
      }
      // Check
      _ <- (1 to 2).toList.traverse { _ =>
        resource.testClient.expect[String](request).attempt.map(r =>
          expect(r.swap.toOption.get.isInstanceOf[RejectedExecution])
        )
      }
      _ <- IO.sleep(1.seconds)
      // Half-open
      _ <- resource.testClient.expect[String](request).attempt.map(r =>
        expect(r.swap.toOption.get.isInstanceOf[TimeoutException])
      )
      // Open again
      _ <- IO.sleep(2.seconds)
      // Half-open
      _ <- IO.delay {
        resource.wireMock.removeMappings()
        resource.wireMock.register(
          get(urlEqualTo(testPath))
            .willReturn(
              aResponse()
                .withBody("wire mock response")
                .withStatus(200)
            )
        )
      }
      res <- resource.testClient.expect[String](request)
      // Closed
    } yield expect(res == "wire mock response")
  }
}
