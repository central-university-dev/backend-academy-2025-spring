package tbank.ab.service

import cats.effect.IO
import sttp.tapir.model.UsernamePassword
import tbank.ab.config.AuthConfig
import tbank.ab.domain.auth.AccessToken
import tbank.ab.domain.auth.error.AuthError
import tbank.ab.repository.AuthRepository

trait AuthService[F[_]]:
  def login(userpass: UsernamePassword): F[Either[AuthError, Unit]]
  def generateToken(userpass: UsernamePassword): F[AccessToken]
  def authenticate(token: AccessToken): F[Either[AuthError, Unit]]

object AuthService:

  final private class Impl(repo: AuthRepository[IO], config: AuthConfig)
      extends AuthService[IO]:
    override def login(
      userpass: UsernamePassword
    ): IO[Either[AuthError, Unit]] =
      IO.pure {
        Either.cond(
          userpass.username == config.login &&
          userpass.password.contains(config.password),
          right = (),
          left = AuthError()
        )
      }

    override def generateToken(userpass: UsernamePassword): IO[AccessToken] =
      ???

    override def authenticate(token: AccessToken): IO[Either[AuthError, Unit]] =
      ???

  def make(repo: AuthRepository[IO], config: AuthConfig): AuthService[IO] =
    Impl(repo, config)
