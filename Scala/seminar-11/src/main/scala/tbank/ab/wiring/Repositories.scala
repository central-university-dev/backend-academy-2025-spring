package tbank.ab.wiring

import cats.Applicative
import cats.arrow.FunctionK
import cats.effect.{Async, MonadCancelThrow}
import tbank.ab.config.AppConfig
import tbank.ab.db.DatabaseModule
import tbank.ab.repository.{AnimalRepository, AuthRepository}
import cats.implicits.*
import tofu.lift.Lift
import cats.tagless.syntax.functorK.*

final case class Repositories[F[_]]()(using
                                      val animalRepo: AnimalRepository[F],
                                      val authRepo: AuthRepository[F]
)

object Repositories:
  def make[I[_]: MonadCancelThrow, F[_]: Async](using db: DatabaseModule[I], config: AppConfig, clients: Clients[F], fk: FunctionK[I, F]): I[Repositories[F]] = {
    import clients.given
    import config.given

    given DatabaseModule[F] = DatabaseModule.mapK[I,  F](db)(fk)
    given AnimalRepository[F] = AnimalRepository.make[F]
    given AuthRepository[F] = AuthRepository.make[F]

    Repositories().pure[I]
  }
