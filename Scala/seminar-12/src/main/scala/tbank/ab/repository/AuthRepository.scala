package tbank.ab.repository

import cats.Monad
import cats.effect.Clock
import cats.effect.Ref
import cats.effect.Sync
import cats.syntax.all.*
import tbank.ab.config.{AppConfig, AuthConfig}
import tbank.ab.domain.auth.{AccessToken, TokenInfo}

import java.time.Instant

trait AuthRepository[F[_]]:
  def find(token: AccessToken): F[Option[AccessToken]]
  def set(token: AccessToken): F[Unit]

object AuthRepository:
  final private class InMemory[F[_]: Monad: Clock](
    repo: Ref[F, Map[AccessToken, TokenInfo]],
    config: AuthConfig
  ) extends AuthRepository[F]:

    override def find(token: AccessToken): F[Option[AccessToken]] =
      for
        r <- repo.get
        tokenInfo = r.get(token)
        now       = Instant.now()
      yield Option.when(
        tokenInfo.exists(_.expiresIn.isAfter(now))
      )(token)

    override def set(token: AccessToken): F[Unit] =
      for
        now <- Clock[F].realTimeInstant
        expiresIn = now.plusSeconds(config.maxAge)
        _ <- repo.update(map => map.updated(token, TokenInfo(expiresIn)))
      yield ()

  def make[I[_]: Sync, F[_]: Sync: Clock](using config: AppConfig): I[AuthRepository[F]] =
    for ref <- Ref.in[I, F, Map[AccessToken, TokenInfo]](Map.empty)
    yield InMemory(ref, config.auth)
