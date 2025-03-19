package tbank.ab.domain

import dev.profunktor.redis4cats.codecs.splits.SplitEpi
import sttp.tapir.Codec

import java.time.Instant

object auth {

  opaque type AccessToken = String

  object AccessToken {
    def apply(token: String): AccessToken            = token
    extension (token: AccessToken) def value: String = token

    given (using c: Codec.PlainCodec[String]): Codec.PlainCodec[AccessToken] = c

    val stringAccessTokenEpi: SplitEpi[String, AccessToken] = SplitEpi(AccessToken(_), _.value)
  }

  final case class TokenInfo(
    expiresIn: Instant
  )

  object TokenInfo {

    def deserialize(expiresIn: String): TokenInfo = TokenInfo(Instant.ofEpochMilli(expiresIn.toLong))

    extension (tokenInfo: TokenInfo) def serialize: String = tokenInfo.expiresIn.toEpochMilli().toString()

    val stringTokenInfoEpi: SplitEpi[String, TokenInfo] = SplitEpi(TokenInfo.deserialize(_), _.serialize)
  }

  object error {
    final case class AuthError()
  }
}
