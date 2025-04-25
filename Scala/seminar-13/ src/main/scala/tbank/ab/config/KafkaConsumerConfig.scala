package tbank.ab.config

import pureconfig.ConfigReader

final case class KafkaConsumerConfig(
  topic: String,
  properties: Map[String, String]
) derives ConfigReader
