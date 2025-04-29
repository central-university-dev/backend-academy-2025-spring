package tbank.ab.wiring

import cats.{~>, Applicative}
import cats.arrow.FunctionK
import cats.effect.{Async, MonadCancelThrow, Sync}
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
  def make[F[_]: Async](using
    db: DatabaseModule[F],
    config: AppConfig,
    clients: Clients[F]
  ): F[Repositories[F]] = {
    import clients.given
    import config.given

    for {
      given AuthRepository[F] <- AuthRepository.make[F]
      given AnimalRepository[F] = AnimalRepository.make[F]
    } yield Repositories()

  }
