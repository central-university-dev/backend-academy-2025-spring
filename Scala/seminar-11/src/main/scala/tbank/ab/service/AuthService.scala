package tbank.ab.service

import cats.Monad
import sttp.tapir.model.UsernamePassword
import tbank.ab.config.AuthConfig
import tbank.ab.domain.auth.AccessToken
import tbank.ab.domain.auth.error.AuthError
import tbank.ab.repository.AuthRepository
import cats.implicits.*
import java.util.Base64

trait AuthService[F[_]]:
  def login(userpass: UsernamePassword): F[Either[AuthError, Unit]]
  def generateToken(userpass: UsernamePassword): F[AccessToken]
  def authenticate(token: AccessToken): F[Either[AuthError, Unit]]

object AuthService:

  final private class Impl[F[_]](repo: AuthRepository[F], config: AuthConfig)(using F: Monad[F]) extends AuthService[F]:
    override def login(
      userpass: UsernamePassword
    ): F[Either[AuthError, Unit]] =
      F.pure {
        Either.cond(
          userpass.username == config.login &&
          userpass.password.contains(config.password),
          right = (),
          left = AuthError()
        )
      }

    private val base64encoder = Base64.getEncoder
    private def base64(userpass: UsernamePassword): AccessToken =
      AccessToken(
        base64encoder.encodeToString(
          s"${userpass.username}:${userpass.password}".getBytes
        )
      )

    override def generateToken(userpass: UsernamePassword): F[AccessToken] =
      for
        token <- F.pure(base64(userpass))
        _     <- repo.set(token)
      yield token

    override def authenticate(token: AccessToken): F[Either[AuthError, Unit]] =
      for token <- repo.find(token)
      yield token
        .map(_ => ())
        .toRight(AuthError())

  def make[F[_]: Monad](using config: AuthConfig, repo: AuthRepository[F]): AuthService[F] =
    Impl(repo, config)
