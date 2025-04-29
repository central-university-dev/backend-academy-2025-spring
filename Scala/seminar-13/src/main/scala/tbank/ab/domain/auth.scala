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

    val stringAccessTokenEpi: SplitEpi[String, AccessToken] = SplitEpi(AccessToken(_), identity)
  }

  final case class TokenInfo(
    expiresIn: Instant
  )

  object TokenInfo {
    val stringTokenInfoEpi: SplitEpi[String, TokenInfo] = SplitEpi(TokenInfo.deserialize, serialize)

    private def deserialize(expiresIn: String): TokenInfo = TokenInfo(Instant.ofEpochMilli(expiresIn.toLong))
    private def serialize(tokenInfo: TokenInfo): String   = tokenInfo.expiresIn.toEpochMilli.toString
  }

  object error {
    final case class AuthError()
  }
}
