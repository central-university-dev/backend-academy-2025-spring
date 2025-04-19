package tbank.ab.controller

import cats.effect.IO
import cats.syntax.either.given
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.*
import sttp.tapir.server.ServerEndpoint
import tbank.ab.controller.endpoints.AnimalEndpoints
import tbank.ab.service.{AnimalService, AuthService}

private class AnimalController(
  animalService: AnimalService[IO],
  authService: AuthService[IO]
) extends Controller[IO] {

  private val allAnimals: ServerEndpoint[Fs2Streams[IO], IO] =
    AnimalEndpoints.allAnimals
      .serverLogicSuccessPure(_ => animalService.allAnimals)

  private val animalDescription: ServerEndpoint[Any, IO] =
    AnimalEndpoints.animalDescription
      .serverLogic { id =>
        for
          description <- animalService.animalDescription(id)
          response = description.toRight("Animal not found")
        yield response
      }

  private val animalInfo: ServerEndpoint[Any, IO] =
    AnimalEndpoints.animalInfo
      .serverLogic { id =>
        for
          info <- animalService.animalInfo(id)
          response = info.toRight("Animal not found")
        yield response
      }

  private val updateAnimalInfo: ServerEndpoint[Any, IO] =
    AnimalEndpoints.updateAnimalInfoEndpoint
      .serverSecurityLogic(userpass =>
        for out <- authService.login(userpass)
        yield out.leftMap(e => "Failed to authenticate")
      )
      .serverLogicSuccess { _ =>
        { case (id, info) =>
          animalService.updateAnimalInfo(id, info)
        }
      }

  override val endpoints: List[ServerEndpoint[Fs2Streams[IO], IO]] =
    List(animalDescription, animalInfo, updateAnimalInfo, allAnimals)
      .map(_.withTag("Animals"))
}

object AnimalController {
  def make(using animalService: AnimalService[IO], authService: AuthService[IO]): Controller[IO] =
    new AnimalController(animalService, authService)
}
