package connectionapi.routes.http

import connectionapi.developerconnection.domain.developerconnection.DeveloperName

object PathVariables {
  object DeveloperNameVar {
    def unapply(str: String): Option[DeveloperName] =
      if (str.nonEmpty)
        Some(DeveloperName.apply(str))
      else
        None
  }
}
