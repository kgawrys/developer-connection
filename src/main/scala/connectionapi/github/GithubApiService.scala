package connectionapi.github

import cats.effect.Async
import connectionapi.github.config.GithubConfig
import connectionapi.github.domain.dto.DeveloperOrganizationResponse
import org.http4s.client.Client
import org.typelevel.log4cats.Logger

trait GithubApiService[F[_]] {
  def getOrganizations: F[DeveloperOrganizationResponse]
}

object GithubApiService {
  def make[F[_]: Async: Logger](
      client: Client[F],
      config: GithubConfig
  ): GithubApiService[F] =
    new GithubApiService[F] {
      def getOrganizations: F[DeveloperOrganizationResponse] = ???
    }

}
