package tbank.ab.wiring

import cats.{~>, Applicative}
import cats.arrow.FunctionK
import cats.effect.{Async, Sync, MonadCancelThrow}
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
  def make[I[_]: Sync, F[_]: Async](using
    db: DatabaseModule[I],
    config: AppConfig,
    clients: Clients[F],
    fk: I ~> F
  ): I[Repositories[F]] = {
    import clients.given
    import config.given

    for {
      given AuthRepository[F]   <- AuthRepository.make[I, F]
      given DatabaseModule[F]   = DatabaseModule.mapK[I, F](db)(fk)
      given AnimalRepository[F] = AnimalRepository.make[F]
    } yield Repositories()
    

    
  }
