package connectionapi.routes.domain

import cats.data.NonEmptyList
import connectionapi.github.domain.GithubResponse.GithubException
import connectionapi.twitter.domain.TwitterResponse.TwitterException
import derevo.circe.magnolia.encoder
import derevo.derive

@derive(encoder)
case class ErrorsResponse(errors: List[String])

// todo: this solution is not leveraging exhaustive pattern matching, can it be done better?
case object ErrorsResponse {
  def apply(errors: NonEmptyList[Throwable]): ErrorsResponse =
    ErrorsResponse(errors.map {
      case GithubException.UserNotFound(msg)    => msg
      case GithubException.APICallFailure(msg)  => msg
      case TwitterException.UserNotFound(msg)   => msg
      case TwitterException.UserIdNotFound(msg) => msg
      case TwitterException.APICallFailure(msg) => msg
      case t @ _                                => t.getClass.getName
    }.toList)
}
