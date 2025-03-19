package tbank.ab.repository

import cats.effect.{IO, Ref}
import dev.profunktor.redis4cats.Redis
import dev.profunktor.redis4cats.RedisCommands
import tbank.ab.config.{AppConfig, AuthConfig}
import tbank.ab.domain.auth.{AccessToken, TokenInfo}

import java.time.Instant

trait AuthRepository[F[_]] {
  def find(token: AccessToken): F[Option[AccessToken]]
  def set(token: AccessToken): F[Unit]
}

object AuthRepository {
  final private class RedisImpl(
    repo: RedisCommands[IO, AccessToken, TokenInfo],
    config: AuthConfig
  ) extends AuthRepository[IO] {

    override def find(token: AccessToken): IO[Option[AccessToken]] =
      for {
        tokenInfo <- repo.get(token)
        now = Instant.now()
      } yield Option.when(
        tokenInfo.exists(_.expiresIn.isAfter(now))
      )(token)

    override def set(token: AccessToken): IO[Unit] =
      for {
        now <- IO.pure(Instant.now())
        expiresIn = now.plusSeconds(config.maxAge)
        _ <- repo.set(token, TokenInfo(expiresIn))
      } yield ()
  }

  def make(using config: AuthConfig, repo: RedisCommands[IO, AccessToken, TokenInfo]): AuthRepository[IO] =
    RedisImpl(repo, config)
}
