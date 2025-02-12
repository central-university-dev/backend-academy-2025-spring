package tbank.ab.controller

import cats.effect.IO
import sttp.tapir.server.ServerEndpoint
import tbank.ab.config.AuthConfig
import tbank.ab.service.AuthService

class AuthController(
  authService: AuthService[IO],
  config: AuthConfig
) extends Controller[IO]:

  private val tokenAuth = ???

  private val cookieAuth = ???

  override def endpoints: List[ServerEndpoint[Any, IO]] =
    List.empty[ServerEndpoint[Any, IO]].map(_.withTag("Auth"))

object AuthController:
  def make(authService: AuthService[IO], config: AuthConfig): AuthController =
    AuthController(authService, config)
