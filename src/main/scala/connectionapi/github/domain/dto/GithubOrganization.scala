package connectionapi.github.domain.dto

import derevo.circe.magnolia.{ decoder, encoder }
import derevo.derive

@derive(encoder, decoder)
case class GithubOrganization(login: String)
