package connectionapi.github

import cats.effect._
import com.github.benmanes.caffeine.cache.Caffeine
import connectionapi.developerconnection.domain.developerconnection.DeveloperName
import connectionapi.github.config.GithubConfig
import connectionapi.github.domain.GithubResponse.GithubException.{ APICallFailure, UserNotFound }
import connectionapi.github.domain.dto.GithubOrganization
import org.http4s.client.Client
import org.http4s.dsl.io.{ ->, /, GET, Ok, Root, _ }
import org.http4s.implicits._
import org.http4s.{ HttpRoutes, Response }
import org.typelevel.log4cats.noop.NoOpLogger
import scalacache.Entry
import scalacache.caffeine.CaffeineCache
import weaver.SimpleIOSuite

import scala.concurrent.duration.DurationInt

object GithubServiceTest extends SimpleIOSuite {

  private val cCache       = Caffeine.newBuilder.build[String, Entry[Seq[GithubOrganization]]]()
  implicit val cache       = CaffeineCache[IO, String, Seq[GithubOrganization]](cCache)
  implicit val lg          = NoOpLogger[IO]
  private val githubConfig = GithubConfig("", 10.minutes)

  private def mockedRoutes(mkResponse: IO[Response[IO]]) =
    HttpRoutes
      .of[IO] {
        case GET -> Root / "users" / _ / "orgs" => mkResponse
      }
      .orNotFound

  test("parse valid response") {
    val client = Client.fromHttpApp(mockedRoutes(Ok.apply("""[{"login": "mustache"}]""")))
    GithubService
      .make[IO](client, githubConfig)
      .getOrganizations(DeveloperName("dev"))
      .map(organizations => expect.same(Seq(GithubOrganization("mustache")), organizations))
  }

  test("handle not found user") {
    val client = Client.fromHttpApp(mockedRoutes(NotFound()))
    GithubService
      .make[IO](client, githubConfig)
      .getOrganizations(DeveloperName("dev"))
      .attempt
      .map {
        case Left(value) => expect(value.isInstanceOf[UserNotFound])
        case Right(_)    => failure("Organizations should be empty")
      }
  }

  test("handle api error") {
    val client = Client.fromHttpApp(mockedRoutes(InternalServerError()))
    GithubService
      .make[IO](client, githubConfig)
      .getOrganizations(DeveloperName("dev"))
      .attempt
      .map {
        case Left(value) => expect(value.isInstanceOf[APICallFailure])
        case Right(_)    => failure("Organizations should be empty")
      }
  }
}
