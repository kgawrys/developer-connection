package connectionapi.developerconnection

import cats.data.Validated.{ Invalid, Valid }
import cats.effect.IO
import cats.instances.string._
import cats.kernel.Eq
import cats.syntax.eq._
import connectionapi.developerconnection.domain.developerconnection.{ Connected, DeveloperName, OrganizationName }
import connectionapi.developerconnection.domain.dto.DeveloperConnectionResponse
import connectionapi.github.GithubService
import connectionapi.github.domain.dto.GithubOrganization
import connectionapi.twitter.TwitterService
import connectionapi.twitter.domain.TwitterDomain
import connectionapi.twitter.domain.TwitterDomain.TwitterUserName
import connectionapi.twitter.domain.dto.{ TwitterUserFollowing, TwitterUserFollowingData, TwitterUserLookup }
import weaver.SimpleIOSuite

object DeveloperConnectionServiceTest extends SimpleIOSuite {

  implicit val developerNameEq: Eq[DeveloperName] = Eq.instance[DeveloperName] { (d1, d2) =>
    d1.value === d2.value
  }

  private def mockedGithubService(
      dev1: DeveloperName,
      dev2: DeveloperName,
      dev1Orgs: Seq[GithubOrganization],
      dev2Orgs: Seq[GithubOrganization]
  ): GithubService[IO] = new GithubService[IO] {
    override def getOrganizations(developerName: DeveloperName): IO[Seq[GithubOrganization]] =
      if (developerName === dev1) IO.pure(dev1Orgs)
      else IO.pure(dev2Orgs)
  }

  private def mockedTwitterService(
      dev1: DeveloperName,
      dev2: DeveloperName,
      dev1Following: TwitterUserFollowing,
      dev2Following: TwitterUserFollowing
  ) =
    new TwitterService[IO] {
      override def userLookup(developerName: DeveloperName): IO[TwitterUserLookup]             = ???
      override def followingById(twitterId: TwitterDomain.TwitterId): IO[TwitterUserFollowing] = ???
      override def followingByDeveloperName(developerName: DeveloperName): IO[TwitterUserFollowing] =
        if (developerName === dev1) IO.pure(dev1Following)
        else IO.pure(dev2Following)
    }

  test("return not connected devs") {
    val dev1 = DeveloperName("dev1")
    val dev2 = DeveloperName("dev2")
    val githubService = mockedGithubService(
      dev1,
      dev2,
      dev1Orgs = Seq(GithubOrganization("org1")),
      dev2Orgs = Seq(GithubOrganization("org2"))
    )
    val twitterService = mockedTwitterService(
      dev1,
      dev2,
      dev1Following = TwitterUserFollowing(Seq(TwitterUserFollowingData(TwitterUserName("unrelated1")))),
      dev2Following = TwitterUserFollowing(Seq(TwitterUserFollowingData(TwitterUserName("unrelated1"))))
    )

    DeveloperConnectionService
      .make[IO](githubService, twitterService)
      .areConnected(dev1, dev2)
      .map {
        case Valid(response) => expect.same(DeveloperConnectionResponse(Connected(false), Seq.empty[OrganizationName]), response)
        case Invalid(errors) => failure("It should return info that users are not connected")
      }
  }

  test("return connected devs") {
    val dev1 = DeveloperName("dev1")
    val dev2 = DeveloperName("dev2")
    val githubService = mockedGithubService(
      dev1,
      dev2,
      dev1Orgs = Seq(GithubOrganization("org1"), GithubOrganization("org2")),
      dev2Orgs = Seq(GithubOrganization("org1"), GithubOrganization("org2"))
    )
    val twitterService = mockedTwitterService(
      dev1,
      dev2,
      dev1Following = TwitterUserFollowing(Seq(TwitterUserFollowingData(TwitterUserName("dev2")))),
      dev2Following = TwitterUserFollowing(Seq(TwitterUserFollowingData(TwitterUserName("dev1"))))
    )

    DeveloperConnectionService
      .make[IO](githubService, twitterService)
      .areConnected(dev1, dev2)
      .map {
        case Valid(response) => expect.same(DeveloperConnectionResponse(Connected(true), Seq(OrganizationName("org1"), OrganizationName("org2"))), response)
        case Invalid(errors) => failure("It should return info that users are not connected")
      }
  }
}
