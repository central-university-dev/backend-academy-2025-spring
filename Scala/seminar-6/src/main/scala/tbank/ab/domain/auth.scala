package tbank.ab.domain

import sttp.tapir.Codec

import java.time.Instant

object auth {

  opaque type AccessToken = String
  object AccessToken:
    def apply(token: String): AccessToken            = token
    extension (token: AccessToken) def value: String = token

    given (using c: Codec.PlainCodec[String]): Codec.PlainCodec[AccessToken] = c

  final case class TokenInfo(
    expiresIn: Instant
  )

  object error {
    final case class AuthError()
  }
}
