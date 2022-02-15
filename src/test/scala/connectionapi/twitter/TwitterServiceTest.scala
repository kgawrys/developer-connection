package connectionapi.twitter

import cats.effect.IO
import com.github.benmanes.caffeine.cache.Caffeine
import connectionapi.developerconnection.domain.developerconnection.DeveloperName
import connectionapi.twitter.config.TwitterConfig
import connectionapi.twitter.domain.TwitterDomain.{ TwitterId, TwitterUserName }
import connectionapi.twitter.domain.TwitterResponse.TwitterException.{ APICallFailure, UserNotFound }
import connectionapi.twitter.domain.dto.{ TwitterUserFollowing, TwitterUserFollowingData, TwitterUserInfo, TwitterUserLookup }
import org.http4s.client.Client
import org.http4s.dsl.io.{ ->, /, GET, Ok, Root, _ }
import org.http4s.implicits._
import org.http4s.{ HttpRoutes, Response }
import org.typelevel.log4cats.noop.NoOpLogger
import scalacache.Entry
import scalacache.caffeine.CaffeineCache
import weaver.SimpleIOSuite

import scala.concurrent.duration.DurationInt

object TwitterServiceTest extends SimpleIOSuite {

  private val cCache        = Caffeine.newBuilder.build[String, Entry[TwitterUserFollowing]]()
  implicit val cache        = CaffeineCache[IO, String, TwitterUserFollowing](cCache)
  implicit val lg           = NoOpLogger[IO]
  private val twitterConfig = TwitterConfig("", "bearer", 10.minutes)

  private def mockedRoutes(lookupResponse: IO[Response[IO]], followingResponse: IO[Response[IO]]) =
    HttpRoutes
      .of[IO] {
        case GET -> Root / "users" / "by" / "username" / _ => lookupResponse
        case GET -> Root / "users" / _ / "following"       => followingResponse
      }
      .orNotFound

  test("parse valid response") {
    val client =
      Client.fromHttpApp(
        mockedRoutes(
          lookupResponse = Ok.apply("""{"data": {"id": "2886589174","username": "dev1"}}"""),
          followingResponse = Ok.apply("""{"data": [{"username": "dev2"}]}""")
        )
      )
    TwitterService
      .make[IO](client, twitterConfig)
      .followingByDeveloperName(DeveloperName("dev1"))
      .map(following => expect.same(TwitterUserFollowing(Seq(TwitterUserFollowingData(TwitterUserName("dev2")))), following))
  }

  test("handle not found user") {
    val client =
      Client.fromHttpApp(
        mockedRoutes(
          lookupResponse = Ok.apply("""{"errors": [{"detail": "Could not find user with username: [dev1].","title": "Not Found Error"}]}"""),
          followingResponse = Ok.apply("""{"data": [{"username": "dev2"}]}""")
        )
      )
    TwitterService
      .make[IO](client, twitterConfig)
      .followingByDeveloperName(DeveloperName("dev1"))
      .attempt
      .map {
        case Left(value) => expect(value.isInstanceOf[UserNotFound])
        case Right(_)    => failure("Following should be empty")
      }
  }

  test("handle api error in second request") {
    val client =
      Client.fromHttpApp(
        mockedRoutes(
          lookupResponse = Ok.apply("""{"data": {"id": "2886589174","username": "dev1"}}"""),
          followingResponse = InternalServerError()
        )
      )
    TwitterService
      .make[IO](client, twitterConfig)
      .followingByDeveloperName(DeveloperName("dev1"))
      .attempt
      .map {
        case Left(value) => expect(value.isInstanceOf[APICallFailure])
        case Right(_)    => failure("Following should be empty")
      }
  }
}
