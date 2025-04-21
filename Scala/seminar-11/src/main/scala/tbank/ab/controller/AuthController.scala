package tbank.ab.controller

import cats.effect.Sync
import cats.syntax.either.given
import sttp.model.headers.CookieValueWithMeta
import sttp.tapir.model.UsernamePassword
import sttp.tapir.server.ServerEndpoint
import tbank.ab.config.AuthConfig
import tbank.ab.controller.endpoints.AuthEndpoints
import tbank.ab.service.AuthService
import cats.implicits.*

private class AuthController[F[_]: Sync](
  authService: AuthService[F],
  config: AuthConfig
) extends Controller[F]:

  private def authenticate(
    userpass: UsernamePassword
  ): F[Either[String, UsernamePassword]] =
    for out <- authService.login(userpass)
    yield out
      .map(_ => userpass)
      .leftMap(e => "Failed to authenticate")

  private val tokenAuth =
    AuthEndpoints.tokenAuth
      .serverSecurityLogic(authenticate)
      .serverLogicSuccess(userpass => _ => authService.generateToken(userpass))

  private val cookieAuth =
    AuthEndpoints.cookieAuth
      .serverSecurityLogic(authenticate)
      .serverLogic { userpass => _ =>
        authService.generateToken(userpass)
          .map { token =>
            CookieValueWithMeta.safeApply(
              value = token.value,
              maxAge = Some(config.maxAge),
              domain = Some("localhost"),
              path = Some("/")
            )
          }
      }

  override def endpoints: List[ServerEndpoint[Any, F]] =
    List(tokenAuth, cookieAuth)
      .map(_.withTag("Auth"))

object AuthController:
  def make[F[_]: Sync](using authService: AuthService[F], config: AuthConfig): Controller[F] =
    new AuthController(authService, config)
