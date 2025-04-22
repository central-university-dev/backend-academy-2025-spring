package tbank.ab.wiring

import cats.Applicative
import cats.arrow.FunctionK
import cats.effect.{Async, MonadCancelThrow}
import cats.implicits.*
import cats.tagless.syntax.functorK.*
import tbank.ab.config.AppConfig
import tbank.ab.db.DatabaseModule
import tbank.ab.domain.RequestContext
import tbank.ab.repository.{AnimalRepository, AuthRepository}
import tofu.WithProvide
import tofu.lift.Lift

final case class Repositories[F[_]]()(using
  val animalRepo: AnimalRepository[F],
  val authRepo: AuthRepository[F]
)

object Repositories:
  def make[I[_]: MonadCancelThrow, F[_]: Async](using
    db: DatabaseModule[I],
    config: AppConfig,
    clients: Clients[F],
    withProvide: WithProvide[F, I, RequestContext]
  ): I[Repositories[F]] = {
    import clients.given
    import config.given

    given DatabaseModule[F]   = DatabaseModule.mapK[I, F](db)(withProvide.liftF)
    given AnimalRepository[F] = AnimalRepository.make[F]
    given AuthRepository[F]   = AuthRepository.make[F]

    Repositories().pure[I]
  }
