package tbank.ab.config

import pureconfig.ConfigReader

final case class KafkaProducerConfig(
  topic: String,
  properties: Map[String, String]
) derives ConfigReader
