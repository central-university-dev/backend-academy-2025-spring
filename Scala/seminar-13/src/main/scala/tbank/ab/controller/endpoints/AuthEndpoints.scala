package tbank.ab.controller.endpoints

import sttp.model.StatusCode
import sttp.model.headers.CookieValueWithMeta
import sttp.tapir.*
import sttp.tapir.model.UsernamePassword
import tbank.ab.domain.auth.AccessToken

object AuthEndpoints:
  private val secureEndpoint: Endpoint[
    UsernamePassword,
    Unit,
    String,
    Unit,
    Any
  ] =
    endpoint.post
      .securityIn(auth.basic[UsernamePassword]())
      .errorOut(stringBody.and(statusCode(StatusCode.Unauthorized)))

  val tokenAuth: Endpoint[UsernamePassword, Unit, String, AccessToken, Any] =
    secureEndpoint
      .summary("bearer auth")
      .in("login" / "token")
      .out(plainBody[AccessToken])

  val cookieAuth: Endpoint[
    UsernamePassword,
    Unit,
    String,
    CookieValueWithMeta,
    Any
  ] =
    secureEndpoint
      .summary("cookie auth")
      .in("login" / "cookie")
      .out(setCookie("session"))
