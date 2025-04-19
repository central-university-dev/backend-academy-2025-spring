package tbank.ab.config

import pureconfig.ConfigReader

final case class RandomCatServiceConfig(
  randomCatFactUri: String,
) derives ConfigReader
