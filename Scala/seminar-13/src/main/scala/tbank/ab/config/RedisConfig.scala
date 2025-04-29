package tbank.ab.config

import pureconfig.ConfigReader

final case class RedisConfig(
  uri: String
) derives ConfigReader
