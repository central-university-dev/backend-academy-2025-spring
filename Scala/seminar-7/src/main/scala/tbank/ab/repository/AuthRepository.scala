package tbank.ab.repository

import cats.effect.{Clock, IO}
import dev.profunktor.redis4cats.RedisCommands
import tbank.ab.config.AuthConfig
import tbank.ab.domain.auth.{AccessToken, TokenInfo}

trait AuthRepository[F[_]] {
  def find(token: AccessToken): F[Option[AccessToken]]
  def set(token: AccessToken): F[Unit]
}

object AuthRepository {

  def make(using
    config: AuthConfig,
    repo: RedisCommands[IO, AccessToken, TokenInfo],
    clock: Clock[IO]
  ): AuthRepository[IO] =
    new RedisImpl

  final private class RedisImpl(using
    repo: RedisCommands[IO, AccessToken, TokenInfo],
    config: AuthConfig,
    clock: Clock[IO]
  ) extends AuthRepository[IO] {

    override def find(token: AccessToken): IO[Option[AccessToken]] =
      for {
        tokenInfo <- repo.get(token)
        now       <- clock.realTimeInstant
      } yield Option.when(tokenInfo.exists(_.expiresIn.isAfter(now)))(token)

    override def set(token: AccessToken): IO[Unit] =
      for {
        now <- clock.realTimeInstant
        expiresIn = now.plusSeconds(config.maxAge)
        _ <- repo.set(token, TokenInfo(expiresIn))
      } yield ()
  }
}
