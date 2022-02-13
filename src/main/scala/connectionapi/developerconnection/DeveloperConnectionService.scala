package connectionapi.developerconnection

import cats.effect.Async
import connectionapi.developerconnection.domain.dto.DeveloperConnectionResponse
import connectionapi.github.GithubApiService
import org.typelevel.log4cats.Logger

trait DeveloperConnectionService[F[_]] {
  def areConnected: F[DeveloperConnectionResponse]
}

object DeveloperConnectionService {

  def make[F[_]: Async: Logger](
      githubApiService: GithubApiService[F]
  ): DeveloperConnectionService[F] =
    new DeveloperConnectionService[F] {
      def areConnected: F[DeveloperConnectionResponse] = ???
    }

}
