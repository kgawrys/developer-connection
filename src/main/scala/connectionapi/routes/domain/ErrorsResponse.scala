package connectionapi.routes.domain

import cats.data.NonEmptyList
import connectionapi.github.domain.GithubResponse.GithubException.{ APICallFailure, UserNotFound }
import derevo.circe.magnolia.encoder
import derevo.derive

@derive(encoder)
case class ErrorsResponse(errors: List[String])

// todo: this solution is not leveraging exhaustive pattern matching, can it be done better?
case object ErrorsResponse {
  def apply(errors: NonEmptyList[Throwable]): ErrorsResponse =
    ErrorsResponse(errors.map {
      case UserNotFound(msg)   => msg
      case APICallFailure(msg) => msg
      case t @ _               => t.getClass.getName
    }.toList)
}
