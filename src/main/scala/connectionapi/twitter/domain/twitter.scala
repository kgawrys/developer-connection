package connectionapi.twitter.domain

import derevo.circe.magnolia.decoder
import derevo.derive
import io.estatico.newtype.macros.newtype

object twitter {
  @derive(decoder)
  @newtype
  case class ErrorDetail(value: String)

  @derive(decoder)
  @newtype
  case class ErrorTitle(value: String)

  @derive(decoder)
  @newtype
  case class TwitterId(value: Int)

  @derive(decoder)
  @newtype
  case class TwitterUserName(value: String)
}
