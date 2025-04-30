package tbank.ab.service

import cats.{Apply, Monad, MonadThrow}
import cats.syntax.applicative.*
import cats.syntax.apply.*
import cats.syntax.functor.*
import cats.tagless.*
import cats.tagless.macros.*
import cats.tagless.syntax.all.*
import fs2.{Chunk, Stream}
import org.typelevel.otel4s.{Attribute, AttributeKey, Attributes}
import org.typelevel.otel4s.trace.Tracer
import tbank.ab.domain.RequestContext
import tbank.ab.domain.animal.{AnimalId, AnimalInfo}
import tbank.ab.repository.AnimalRepository
import tofu.higherKind.Mid
import tofu.logging.Logging
import tofu.syntax.logging.*

import java.nio.charset.StandardCharsets

trait AnimalService[F[_]] derives FunctorK, ApplyK {
  def animalDescription(id: AnimalId): F[Option[String]]
  def animalInfo(id: AnimalId): F[Option[AnimalInfo]]
  def updateAnimalInfo(id: AnimalId, info: AnimalInfo): F[AnimalInfo]
  def randomCat(): F[String]
}

object AnimalService {

  def make[F[_]: MonadThrow: Logging.Make](using
    repo: AnimalRepository[F],
    randomCatService: RandomCatService[F]
  ): AnimalService[F] = {
    given Logging[F]                   = Logging.Make[F].forService[AnimalService.type]
    val logs: AnimalService[Mid[F, *]] = new AnimalServiceLogs[F]
    logs attach new AnimalServiceImpl(repo, randomCatService)
  }

  extension[F[_]: Tracer] (service: AnimalService[F]) {
    def withTracing: AnimalService[F] = {
      val tracing: AnimalService[Mid[F, *]] = new AnimalServiceTracing[F]
      tracing attach service
    }
  }

  final private class AnimalServiceImpl[F[_]: Monad](repo: AnimalRepository[F], randomCatService: RandomCatService[F])
      extends AnimalService[F] {

    override def animalDescription(id: AnimalId): F[Option[String]] =
      repo.find(id)
        .map(_.map(_.description))

    override def animalInfo(id: AnimalId): F[Option[AnimalInfo]] =
      repo.find(id)

    override def updateAnimalInfo(
      id: AnimalId,
      info: AnimalInfo
    ): F[AnimalInfo] =
      repo.update(id, info)

    override def randomCat(): F[String] = randomCatService.randomCatFact()
  }

  final private class AnimalServiceLogs[F[_]: Apply: Logging] extends AnimalService[Mid[F, *]] {
    def animalDescription(id: AnimalId): Mid[F, Option[String]] =
      _ <* debugWith"Found animal description" ("animalId" -> id)

    def animalInfo(id: AnimalId): Mid[F, Option[AnimalInfo]] =
      _ <* debugWith"Found animal info" ("animalId" -> id)

    def updateAnimalInfo(id: AnimalId, info: AnimalInfo): Mid[F, AnimalInfo] =
      _ <* debugWith"Updated animal info" ("animalId" -> id)

    def randomCat(): Mid[F, String] =
      _ <* debug"Return random cat fact"
  }

  final private class AnimalServiceTracing[F[_]: Tracer] extends AnimalService[Mid[F, *]] {
    def animalDescription(id: AnimalId): Mid[F, Option[String]] =
      Tracer[F].span("animalDescription", Attribute.from(id)).surround(_)

    def animalInfo(id: AnimalId): Mid[F, Option[AnimalInfo]] =
      Tracer[F].span("animalInfo", Attribute.from(id)).surround(_)

    def updateAnimalInfo(id: AnimalId, info: AnimalInfo): Mid[F, AnimalInfo] =
      Tracer[F].span("updateAnimalInfo", Attribute.from(id)).surround(_)

    def randomCat(): Mid[F, String] = Tracer[F].span("randomCat").surround(_)
  }

}
