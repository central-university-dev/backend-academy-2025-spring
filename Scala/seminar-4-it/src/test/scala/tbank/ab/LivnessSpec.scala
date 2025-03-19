package tbank.ab

import cats.effect.*
import cats.syntax.traverse.*
import org.mockserver.matchers.Times
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import sttp.client3.{basicRequest, SttpBackend}
import sttp.model.Uri
import weaver.*

class LivnessSpec(global: GlobalRead) extends IOSuite {

  override type Res = MyResources

  override def sharedResource: Resource[IO, MyResources] =
    global.getOrFailR[MyResources]()

  test("Livness should be working") { (resource: MyResources) =>
    for {
      response <- callLiveness(resource.testClient, resource.appHost)
    } yield expect(response == Right(""))
  }

  test("Mock livness should be working") { (resource: MyResources) =>
    Resource.make(
      // Set up expectation
      IO(
        resource.mockClient
          .when(
            request("/live")
              .withMethod("GET"),
            Times.once()
          )
          .respond(
            response()
              .withBody("Ok")
              .withStatusCode(200)
          )
          .toList
      )
    )( // clean up expectation
      _.traverse(expectation => IO(resource.mockClient.clear(expectation.getId))).void
    ).use { _ =>
      // Use logic which requires the mock server
      for {
        response <- callLiveness(resource.testClient, resource.mockHost)
      } yield expect(response == Right("Ok"))
    }
  }

  private def callLiveness(
    client: SttpBackend[IO, Any],
    host: Uri
  ): IO[Either[String, String]] =
    client.send(basicRequest.get(host.addPath("live")))
      .map(_.body)

}
