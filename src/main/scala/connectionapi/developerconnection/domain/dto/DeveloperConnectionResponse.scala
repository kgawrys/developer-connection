package connectionapi.developerconnection.domain.dto

import connectionapi.developerconnection.domain.developerconnection.{ Connected, OrganizationName }
import derevo.circe.magnolia.{ decoder, encoder }
import derevo.derive

@derive(decoder, encoder)
case class DeveloperConnectionResponse(connected: Connected, organisations: Seq[OrganizationName])
