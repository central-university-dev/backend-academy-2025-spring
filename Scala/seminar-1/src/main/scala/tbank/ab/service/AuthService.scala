package tbank.ab.service

import cats.effect.IO
import sttp.tapir.model.UsernamePassword
import tbank.ab.config.AuthConfig
import tbank.ab.domain.auth.AuthError

trait AuthService[F[_]]:
  def login(usernamePassword: UsernamePassword): F[Either[AuthError, Unit]]

object AuthService:

  final private class Impl(config: AuthConfig) extends AuthService[IO]:
    override def login(
      usernamePassword: UsernamePassword
    ): IO[Either[AuthError, Unit]] =
      IO.pure {
        Either.cond(
          usernamePassword.username == config.login &&
          usernamePassword.password.contains(config.password),
          right = (),
          left = AuthError()
        )
      }

  def make(config: AuthConfig): AuthService[IO] =
    Impl(config)
