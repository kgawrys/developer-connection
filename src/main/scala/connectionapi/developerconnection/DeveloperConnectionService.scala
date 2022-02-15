package connectionapi.developerconnection

import cats.data.Validated._
import cats.data.ValidatedNel
import cats.effect.Async
import cats.effect.implicits._
import cats.implicits.{ catsSyntaxApplicativeError, _ }
import connectionapi.developerconnection.domain.developerconnection.{ Connected, DeveloperName, OrganizationName }
import connectionapi.developerconnection.domain.dto.DeveloperConnectionResponse
import connectionapi.github.GithubService
import connectionapi.github.domain.dto.GithubOrganization
import connectionapi.twitter.TwitterService
import connectionapi.twitter.domain.dto.TwitterUserFollowing
import org.typelevel.log4cats.Logger

trait DeveloperConnectionService[F[_]] {
  def areConnected(devName1: DeveloperName, devName2: DeveloperName): F[ValidatedNel[Throwable, DeveloperConnectionResponse]]
}

object DeveloperConnectionService {
  def make[F[_]: Async: Logger](
      githubService: GithubService[F],
      twitterService: TwitterService[F]
  ): DeveloperConnectionService[F] =
    new DeveloperConnectionService[F] {

      def areConnected(devName1: DeveloperName, devName2: DeveloperName): F[ValidatedNel[Throwable, DeveloperConnectionResponse]] = {
        val dev1Orgs      = githubService.getOrganizations(devName1).attempt
        val dev2Orgs      = githubService.getOrganizations(devName2).attempt
        val dev1Following = twitterService.followingByDeveloperName(devName1).attempt
        val dev2Following = twitterService.followingByDeveloperName(devName2).attempt

        (dev1Orgs, dev2Orgs, dev1Following, dev2Following).parMapN { (dev1Orgs, dev2Orgs, dev1Following, dev2Following) =>
          handleResult(devName1, devName2, dev1Orgs, dev2Orgs, dev1Following, dev2Following)
        }
      }

      // This is for accumulative error handling
      private def handleResult(
          devName1: DeveloperName,
          devName2: DeveloperName,
          dev1Orgs: Either[Throwable, Seq[GithubOrganization]],
          dev2Orgs: Either[Throwable, Seq[GithubOrganization]],
          dev1Following: Either[Throwable, TwitterUserFollowing],
          dev2Following: Either[Throwable, TwitterUserFollowing]
      ): ValidatedNel[Throwable, DeveloperConnectionResponse] =
        (
          dev1Orgs.toValidatedNel,
          dev2Orgs.toValidatedNel,
          dev1Following.toValidatedNel,
          dev2Following.toValidatedNel
        ).mapN { (dev1Orgs, dev2Orgs, dev1Following, dev2Following) =>
          resolveConnection(devName1, devName2, dev1Orgs, dev2Orgs, dev1Following, dev2Following)
        }

      private def resolveConnection(
          developerName1: DeveloperName,
          developerName2: DeveloperName,
          dev1Orgs: Seq[GithubOrganization],
          dev2Orgs: Seq[GithubOrganization],
          dev1Following: TwitterUserFollowing,
          dev2Following: TwitterUserFollowing
      ): DeveloperConnectionResponse = {
        val commonOrganizations = dev1Orgs.map(_.login).intersect(dev2Orgs.map(_.login))
        if (
          dev1Following.data.map(_.username.value).contains(developerName2.value) &&
          dev2Following.data.map(_.username.value).contains(developerName1.value) &&
          commonOrganizations.nonEmpty
        ) {
          DeveloperConnectionResponse(Connected(true), commonOrganizations.map(OrganizationName(_)))
        } else
          DeveloperConnectionResponse(Connected(false), Nil)
      }

    }

}
