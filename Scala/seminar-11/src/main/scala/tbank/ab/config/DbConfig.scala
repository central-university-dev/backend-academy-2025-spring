package tbank.ab.config

import com.zaxxer.hikari.HikariConfig
import pureconfig.ConfigReader

case class DbConfig(
  driver: String,
  url: String,
  user: String,
  password: String
) derives ConfigReader

object DbConfig {

  def toHikariConf(config: DbConfig): HikariConfig = {
    val hikariConfig = new HikariConfig()
    hikariConfig.setDriverClassName(config.driver)
    hikariConfig.setJdbcUrl(config.url)
    hikariConfig.setUsername(config.user)
    hikariConfig.setPassword(config.password)
    hikariConfig
  }

}
