package tbank.ab.repository

import cats.effect.{IO, Ref}
import tbank.ab.config.{AppConfig, AuthConfig}
import tbank.ab.domain.auth.{AccessToken, TokenInfo}

import java.time.Instant

trait AuthRepository[F[_]] {
  def find(token: AccessToken): F[Option[AccessToken]]
  def set(token: AccessToken): F[Unit]
}

object AuthRepository {
  final private class InMemory(
    repo: Ref[IO, Map[AccessToken, TokenInfo]],
    config: AuthConfig
  ) extends AuthRepository[IO] {

    override def find(token: AccessToken): IO[Option[AccessToken]] =
      for {
        r <- repo.get
        tokenInfo = r.get(token)
        now       = Instant.now()
      } yield Option.when(
        tokenInfo.exists(_.expiresIn.isAfter(now))
      )(token)

    override def set(token: AccessToken): IO[Unit] =
      for {
        now <- IO.pure(Instant.now())
        expiresIn = now.plusSeconds(config.maxAge)
        _ <- repo.update(map => map.updated(token, TokenInfo(expiresIn)))
      } yield ()
  }

  def make(using config: AuthConfig): IO[AuthRepository[IO]] =
    Ref.of[IO, Map[AccessToken, TokenInfo]](Map.empty)
      .map(InMemory(_, config))
}
