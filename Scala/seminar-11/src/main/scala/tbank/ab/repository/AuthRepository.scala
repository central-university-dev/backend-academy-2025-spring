package tbank.ab.repository

import cats.Monad
import cats.effect.Clock
import cats.implicits.*
import dev.profunktor.redis4cats.RedisCommands
import tbank.ab.config.AuthConfig
import tbank.ab.domain.auth.{AccessToken, TokenInfo}

trait AuthRepository[F[_]] {
  def find(token: AccessToken): F[Option[AccessToken]]
  def set(token: AccessToken): F[Unit]
}

object AuthRepository {

  def make[F[_]: Monad](using
    config: AuthConfig,
    repo: RedisCommands[F, AccessToken, TokenInfo],
    clock: Clock[F]
  ): AuthRepository[F] =
    new RedisImpl[F]

  final private class RedisImpl[F[_]: Monad](using
    repo: RedisCommands[F, AccessToken, TokenInfo],
    config: AuthConfig,
    clock: Clock[F]
  ) extends AuthRepository[F] {

    override def find(token: AccessToken): F[Option[AccessToken]] =
      for {
        tokenInfo <- repo.get(token)
        now       <- clock.realTimeInstant
      } yield Option.when(tokenInfo.exists(_.expiresIn.isAfter(now)))(token)

    override def set(token: AccessToken): F[Unit] =
      for {
        now <- clock.realTimeInstant
        expiresIn = now.plusSeconds(config.maxAge)
        _ <- repo.set(token, TokenInfo(expiresIn))
      } yield ()
  }
}
