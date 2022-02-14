package connectionapi.developerconnection

import cats.data.Validated._
import cats.data.ValidatedNel
import cats.effect.Async
import cats.implicits.{ catsSyntaxApplicativeError, _ }
import connectionapi.developerconnection.domain.developerconnection.{ Connected, DeveloperName, OrganizationName }
import connectionapi.developerconnection.domain.dto.DeveloperConnectionResponse
import connectionapi.github.GithubApiService
import connectionapi.github.domain.dto.GithubOrganization
import org.typelevel.log4cats.Logger
trait DeveloperConnectionService[F[_]] {
  def areConnected(devName1: DeveloperName, devName2: DeveloperName): F[ValidatedNel[Throwable, DeveloperConnectionResponse]]
}

object DeveloperConnectionService {
  def make[F[_]: Async: Logger](
      githubApiService: GithubApiService[F]
  ): DeveloperConnectionService[F] =
    new DeveloperConnectionService[F] {
      def areConnected(devName1: DeveloperName, devName2: DeveloperName): F[ValidatedNel[Throwable, DeveloperConnectionResponse]] =
        for {
          dev1OrgsEither <- githubApiService.getOrganizations(devName1).attempt
          dev2OrgsEither <- githubApiService.getOrganizations(devName2).attempt
          _              <- Logger[F].info(s"devName1: ${devName1.value}, devName2: ${devName2.value}") // todo remove this logger
        } yield handleResult(dev1OrgsEither, dev2OrgsEither)

      // todo type alias for either?
      def handleResult(
          dev1Orgs: Either[Throwable, Seq[GithubOrganization]],
          dev2Orgs: Either[Throwable, Seq[GithubOrganization]]
      ): ValidatedNel[Throwable, DeveloperConnectionResponse] =
        (
          dev1Orgs.toValidatedNel,
          dev2Orgs.toValidatedNel
        ).mapN { (dev1Orgs, dev2Orgs) =>
          println(s"dev1ORgs: $dev1Orgs, dev2Orgs: $dev2Orgs")
          DeveloperConnectionResponse(Connected(false), Seq.empty[OrganizationName])
        }

    }

}
