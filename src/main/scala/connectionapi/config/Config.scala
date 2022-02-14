package connectionapi.config

import cats.effect.Sync
import com.comcast.ip4s.{ Host, Port }
import connectionapi.config.Config.{ AppConfig, HttpClientConfig }
import connectionapi.github.config.GithubConfig
import connectionapi.twitter.config.TwitterConfig
import pureconfig.error.CannotConvert
import pureconfig.generic.auto._
import pureconfig.{ ConfigCursor, ConfigReader, ConfigSource }

import scala.concurrent.duration.FiniteDuration

case class Config(
    app: AppConfig,
    httpClient: HttpClientConfig,
    githubConfig: GithubConfig,
    twitterConfig: TwitterConfig
)

object Config {

  implicit val HostReader: ConfigReader[Host] = new ConfigReader[Host] {
    def from(cur: ConfigCursor) = cur.asString.flatMap(s =>
      Host.fromString(s) match {
        case Some(value) => Right(value)
        case None        => cur.failed(CannotConvert(s, "Host", "Cannot be converted"))
      }
    )
  }

  implicit val PortReader: ConfigReader[Port] = new ConfigReader[Port] {
    def from(cur: ConfigCursor) = cur.asInt.flatMap(i =>
      Port.fromInt(i) match {
        case Some(value) => Right(value)
        case None        => cur.failed(CannotConvert(i.toString, "Port", "Cannot be converted"))
      }
    )
  }

  case class AppConfig(
      host: Host,
      port: Port
  )

  case class HttpClientConfig(
      timeout: FiniteDuration,
      idleTimeInPool: FiniteDuration
  )

  def load[F[_]: Sync]: F[Config] =
    Sync[F].delay(ConfigSource.default.loadOrThrow[Config])

}
