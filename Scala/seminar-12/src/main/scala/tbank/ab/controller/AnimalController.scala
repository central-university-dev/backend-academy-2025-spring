package tbank.ab.controller

import cats.MonadThrow
import cats.implicits.*
import cats.syntax.either.given
import sttp.capabilities.fs2.Fs2Streams
import sttp.tapir.*
import sttp.tapir.server.ServerEndpoint
import tbank.ab.controller.endpoints.AnimalEndpoints
import tbank.ab.service.{AnimalService, AuthService}

private class AnimalController[F[_]: MonadThrow](
  animalService: AnimalService[F],
  authService: AuthService[F]
) extends Controller[F] {

  private val animalDescription: ServerEndpoint[Any, F] =
    AnimalEndpoints.animalDescription
      .serverLogic { id =>
        for
          description <- animalService.animalDescription(id)
          response = description.toRight("Animal not found")
        yield response
      }

  private val animalInfo: ServerEndpoint[Any, F] =
    AnimalEndpoints.animalInfo
      .serverLogic { id =>
        for
          info <- animalService.animalInfo(id)
          response = info.toRight("Animal not found")
        yield response
      }

  private val updateAnimalInfo: ServerEndpoint[Any, F] =
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

  private val randomFact: ServerEndpoint[Any, F] =
    AnimalEndpoints.randomFact
      .serverLogic(_ =>
        animalService.randomCat().attempt.map(
          _.left.map(_.getMessage)
        )
      )

  override val endpoints: List[ServerEndpoint[Any, F]] =
    List(animalDescription, animalInfo, updateAnimalInfo, randomFact)
      .map(_.withTag("Animals"))
}

object AnimalController {
  def make[F[_]: MonadThrow](using animalService: AnimalService[F], authService: AuthService[F]): Controller[F] =
    new AnimalController(animalService, authService)
}
