package tbank.ab

import cats.effect.*
import sttp.client3.{asStringAlways, emptyRequest, UriContext}
import weaver.*

class LivnessSpec(global: GlobalRead) extends IOSuite {
  val host = uri"http://localhost:8080"

  type Res = MyResources

  def sharedResource: Resource[IO, MyResources] =
    global.getOrFailR[MyResources](label = Some("my-resource"))

  test("Livness should be working") { (resource: MyResources) =>
    for {
      response <- resource.backend.send(
                    emptyRequest
                      .get(host.addPath("live"))
                      .response(asStringAlways)
                  ).map(_.body)
    } yield expect(response == "")
  }

}
