package connectionapi.twitter.config

import scala.concurrent.duration.FiniteDuration

case class TwitterConfig(baseUri: String, bearer: String, cacheTTL: FiniteDuration)
