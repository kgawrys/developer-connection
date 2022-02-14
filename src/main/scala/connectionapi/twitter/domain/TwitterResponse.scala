package connectionapi.twitter.domain

import scala.util.control.NoStackTrace

object TwitterResponse {
  sealed trait TwitterException extends NoStackTrace {
    val message: String
  }
  object TwitterException {
    case class UserNotFound(message: String) extends TwitterException
    case class UserIdNotFound(message: String) extends TwitterException
    case class APICallFailure(message: String) extends TwitterException
  }
}
