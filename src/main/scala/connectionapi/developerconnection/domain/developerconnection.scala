package connectionapi.developerconnection.domain

import derevo.circe.magnolia.{ decoder, encoder }
import derevo.derive
import io.estatico.newtype.macros.newtype

object developerconnection {
  @newtype
  case class DeveloperName(value: String)

  @derive(decoder, encoder)
  @newtype
  case class Connected(value: Boolean)

  @derive(decoder, encoder)
  @newtype
  case class OrganizationName(value: String)
}
