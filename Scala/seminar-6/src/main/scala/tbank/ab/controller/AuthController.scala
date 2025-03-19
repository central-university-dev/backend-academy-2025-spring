package tbank.ab.controller

import cats.effect.IO
import cats.syntax.either.given
import sttp.model.headers.CookieValueWithMeta
import sttp.tapir.model.UsernamePassword
import sttp.tapir.server.ServerEndpoint
import tbank.ab.config.AuthConfig
import tbank.ab.controller.endpoints.AuthEndpoints
import tbank.ab.service.AuthService

private class AuthController(
  authService: AuthService[IO],
  config: AuthConfig
) extends Controller[IO]:

  private def authenticate(
    userpass: UsernamePassword
  ): IO[Either[String, UsernamePassword]] =
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

  override def endpoints: List[ServerEndpoint[Any, IO]] =
    List(tokenAuth, cookieAuth)
      .map(_.withTag("Auth"))

object AuthController:
  def make(using authService: AuthService[IO], config: AuthConfig): Controller[IO] =
    new AuthController(authService, config)
