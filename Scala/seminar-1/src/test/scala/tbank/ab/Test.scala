package tbank.ab

import cats.effect.{ExitCode, IO, IOApp}

object Test extends IOApp:

  override def run(args: List[String]): IO[ExitCode] = {
    // GET /live (200 OK)
    ???

    // GET /animal/{animal-id}/description (text)
    ???

    // GET /animal/{animal-id} (JSON)
    ???

    // PUT /animal/{animal-id} (JSON + basic auth)
    ???

    // GET /habitat/image?animal-id - (query + binary body)
    ???
  }
