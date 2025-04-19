package tbank.ab.controller

import cats.data.EitherT
import cats.effect.IO
import sttp.capabilities.fs2.Fs2Streams
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.server.ServerEndpoint
import tbank.ab.controller.endpoints.HabitatEndpoints
import tbank.ab.domain.auth.AccessToken
import tbank.ab.service.{AuthService, HabitatService}

private class HabitatController(
  habitatService: HabitatService[IO],
  authService: AuthService[IO]
) extends Controller[IO]:

  private val animalImage: ServerEndpoint[Any, IO] =
    HabitatEndpoints.animalImage
      .serverLogic { animalId =>
        for
          image <- habitatService.findImage(animalId)
          contentDisposition = s"attachment; filename=$animalId.jpg"
          response =
            image.toRight("Animal not found")
              .map(bytes => (bytes, contentDisposition))
        yield response
      }

  private val uploadAnimalImage: ServerEndpoint[Any with Fs2Streams[IO], IO] =
    HabitatEndpoints.uploadAnimalImage
      .serverSecurityLogic {
        case (Some(bearerToken), _) =>
          EitherT(authService.authenticate(bearerToken))
            .leftMap(_ => ("Failed to authenticate", StatusCode.Unauthorized))
            .value
        case (_, Some(cookieToken)) =>
          EitherT(authService.authenticate(AccessToken(cookieToken.value)))
            .leftMap(_ => ("Failed to authenticate", StatusCode.Unauthorized))
            .value
        case _ =>
          IO(
            Left(
              ("Failed to authenticate", StatusCode.Unauthorized)
            )
          )
      }
      .serverLogic { _ =>
        { case (animalId, image) =>
          EitherT(habitatService.uploadImage(animalId, image))
            .leftMap(e => ("Failed to upload image", StatusCode.InternalServerError))
            .value
        }
      }

  override val endpoints: List[ServerEndpoint[Any with Fs2Streams[IO], IO]] =
    List(animalImage, uploadAnimalImage)
      .map(_.withTag("Habitat"))

object HabitatController:
  def make(using
    habitatService: HabitatService[IO],
    authService: AuthService[IO]
  ): Controller[IO] =
    new HabitatController(habitatService, authService)
