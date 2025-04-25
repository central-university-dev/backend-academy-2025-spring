package tbank.ab.config

final case class ServerConfig(
  monitoring: ZoneConfig,
  public: ZoneConfig
)
