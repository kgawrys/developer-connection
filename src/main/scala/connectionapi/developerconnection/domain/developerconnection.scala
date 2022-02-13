package connectionapi.developerconnection.domain

import io.estatico.newtype.macros.newtype

object developerconnection {
  @newtype
  case class DeveloperName(value: String)

}
