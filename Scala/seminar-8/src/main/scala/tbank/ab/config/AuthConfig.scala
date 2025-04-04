package tbank.ab.config

import pureconfig.ConfigReader

final case class AuthConfig(
  login: String,
  password: String,
  maxAge: Long
) derives ConfigReader
