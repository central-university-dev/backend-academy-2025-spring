package tbank.ab.config

import pureconfig.ConfigReader

final case class AuthConfig(
  login: String,
  password: String
) derives ConfigReader
