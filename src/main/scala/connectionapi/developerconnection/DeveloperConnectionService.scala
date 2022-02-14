package connectionapi.developerconnection

import cats.data.Validated._
import cats.data.ValidatedNel
import cats.effect.Async
import cats.implicits.{ catsSyntaxApplicativeError, _ }
import connectionapi.developerconnection.domain.developerconnection.{ Connected, DeveloperName, OrganizationName }
import connectionapi.developerconnection.domain.dto.DeveloperConnectionResponse
import connectionapi.github.GithubApiService
import connectionapi.github.domain.dto.GithubOrganization
import connectionapi.twitter.TwitterApiService
import connectionapi.twitter.domain.dto.TwitterUserFollowingResponse.TwitterUserFollowing
import connectionapi.twitter.domain.dto.TwitterUserLookupResponse
import org.typelevel.log4cats.Logger
trait DeveloperConnectionService[F[_]] {
  def areConnected(devName1: DeveloperName, devName2: DeveloperName): F[ValidatedNel[Throwable, DeveloperConnectionResponse]]
}

object DeveloperConnectionService {
  def make[F[_]: Async: Logger](
      githubApiService: GithubApiService[F],
      twitterApiService: TwitterApiService[F]
  ): DeveloperConnectionService[F] =
    new DeveloperConnectionService[F] {

      def areConnected(devName1: DeveloperName, devName2: DeveloperName): F[ValidatedNel[Throwable, DeveloperConnectionResponse]] =
        for {
          dev1Orgs      <- githubApiService.getOrganizations(devName1).attempt
          dev2Orgs      <- githubApiService.getOrganizations(devName2).attempt
          dev1Following <- twitterApiService.followingByDeveloperName(devName1).attempt
          dev2Following <- twitterApiService.followingByDeveloperName(devName2).attempt
          _             <- Logger[F].info(s"devName1: ${devName1.value}, devName2: ${devName2.value}") // todo remove this logger
        } yield handleResult(dev1Orgs, dev2Orgs, dev1Following, dev2Following)

      // todo type alias for either?
      def handleResult(
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
        ).mapN { (dev1Orgs, dev2Orgs, dev1TwitterData, dev2TwitterData) =>
          println(s"dev1ORgs: $dev1Orgs, dev2Orgs: $dev2Orgs, dev1Following: $dev1TwitterData, dev2Following: $dev2TwitterData")
          DeveloperConnectionResponse(Connected(false), Seq.empty[OrganizationName])
        }

    }

}
