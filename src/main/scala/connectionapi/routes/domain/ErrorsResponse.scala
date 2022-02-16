package connectionapi.routes.domain

import cats.data.NonEmptyList
import connectionapi.github.domain.GithubResponse.GithubException
import connectionapi.twitter.domain.TwitterResponse.TwitterException
import derevo.circe.magnolia.{ decoder, encoder }
import derevo.derive

@derive(encoder, decoder)
case class ErrorsResponse(errors: List[String])

case object ErrorsResponse {
  def apply(errors: NonEmptyList[Throwable]): ErrorsResponse =
    ErrorsResponse(errors.map {
      case ge: GithubException  => ge.message
      case te: TwitterException => te.message
      case t @ _                => t.getClass.getName
    }.toList)
}
