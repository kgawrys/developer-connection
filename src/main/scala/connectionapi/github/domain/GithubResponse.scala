package connectionapi.github.domain

import scala.util.control.NoStackTrace

object GithubResponse {
  sealed trait GithubException extends NoStackTrace {
    val message: String
  }
  object GithubException {
    case class UserNotFound(message: String) extends GithubException
    case class APICallFailure(message: String) extends GithubException
  }
}
