package connectionapi.twitter.domain.dto

import connectionapi.twitter.domain.TwitterDomain._
import derevo.circe.magnolia.decoder
import derevo.derive

sealed trait TwitterUserFollowingResponse
object TwitterUserFollowingResponse {
  @derive(decoder)
  case class TwitterUserFollowing(data: TwitterUserFollowingData) extends TwitterUserFollowingResponse
}

sealed trait TwitterUserLookupResponse
object TwitterUserLookupResponse {
  @derive(decoder)
  case class TwitterUserLookup(data: TwitterUserInfo) extends TwitterUserLookupResponse
}

@derive(decoder)
case class TwitterError(errors: Seq[UserLookupError])

@derive(decoder)
case class UserLookupError(detail: ErrorDetail, title: ErrorTitle)

@derive(decoder)
case class TwitterUserInfo(id: TwitterId, username: TwitterUserName)

@derive(decoder)
case class TwitterUserFollowingData(username: TwitterUserName)
