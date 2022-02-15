package connectionapi.github.config

import scala.concurrent.duration.FiniteDuration

case class GithubConfig(baseUri: String, cacheTTL: FiniteDuration)
