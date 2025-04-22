package tbank.ab.controller

import cats.MonadThrow
import cats.data.EitherT
import cats.implicits.*
import sttp.capabilities.fs2.Fs2Streams
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.server.ServerEndpoint
import tbank.ab.controller.endpoints.HabitatEndpoints
import tbank.ab.domain.auth.AccessToken
import tbank.ab.service.{AuthService, HabitatService}

private class HabitatController[F[_]: MonadThrow](
  habitatService: HabitatService[F],
  authService: AuthService[F]
) extends Controller[F]:

  private val animalImage: ServerEndpoint[Any, F] =
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

  private val uploadAnimalImage: ServerEndpoint[Any with Fs2Streams[F], F] =
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
          Left(
            ("Failed to authenticate", StatusCode.Unauthorized)
          ).pure[F]

      }
      .serverLogic { _ =>
        { case (animalId, image) =>
          EitherT(habitatService.uploadImage(animalId, image))
            .leftMap(e => ("Failed to upload image", StatusCode.InternalServerError))
            .value
        }
      }

  override val endpoints: List[ServerEndpoint[Any with Fs2Streams[F], F]] =
    List(animalImage, uploadAnimalImage)
      .map(_.withTag("Habitat"))

object HabitatController:
  def make[F[_]: MonadThrow](using
    habitatService: HabitatService[F],
    authService: AuthService[F]
  ): Controller[F] =
    new HabitatController(habitatService, authService)
