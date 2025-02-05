package tbank.ab.controller

import cats.effect.IO
import sttp.tapir.*
import sttp.tapir.server.ServerEndpoint
import tbank.ab.controller.endpoints.HabitatEndpoints
import tbank.ab.service.HabitatService

class HabitatController(
  habitatService: HabitatService[IO]
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

  override val endpoints: List[ServerEndpoint[Any, IO]] =
    List(animalImage)
      .map(_.withTag("Habitat"))

object HabitatController:
  def make(
    habitatService: HabitatService[IO]
  ): HabitatController =
    HabitatController(habitatService)
