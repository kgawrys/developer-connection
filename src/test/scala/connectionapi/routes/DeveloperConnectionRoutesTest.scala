package connectionapi.routes

import cats.data.{ Validated, ValidatedNel }
import cats.effect.IO
import connectionapi.developerconnection.DeveloperConnectionService
import connectionapi.developerconnection.domain.developerconnection
import connectionapi.developerconnection.domain.developerconnection.{ Connected, OrganizationName }
import connectionapi.developerconnection.domain.dto.DeveloperConnectionResponse
import connectionapi.github.domain.GithubResponse.GithubException.UserNotFound
import connectionapi.routes.domain.ErrorsResponse
import connectionapi.utils.HttpTestUtils
import org.http4s.Status
import org.http4s.client.dsl.io._
import org.http4s.dsl.io.GET
import org.http4s.implicits._
import org.typelevel.log4cats.noop.NoOpLogger
import weaver.SimpleIOSuite

object DeveloperConnectionRoutesTest extends SimpleIOSuite with HttpTestUtils {
  implicit val lg = NoOpLogger[IO]

  def mockedDeveloperConnectionService(response: IO[ValidatedNel[Throwable, DeveloperConnectionResponse]]): DeveloperConnectionService[IO] =
    new DeveloperConnectionService[IO] {
      override def areConnected(
          devName1: developerconnection.DeveloperName,
          devName2: developerconnection.DeveloperName
      ): IO[ValidatedNel[Throwable, DeveloperConnectionResponse]] = response
    }

  test("return Ok and valid response when user was found") {
    val req                        = GET(uri"/developers/connected/dev1/dev2")
    val expectedResponse           = DeveloperConnectionResponse(Connected(true), Seq(OrganizationName("org1")))
    val developerConnectionService = mockedDeveloperConnectionService(IO.pure(Validated.valid(expectedResponse)))
    val routes                     = DeveloperConnectionRoutes[IO](developerConnectionService).routes

    expectHttpStatusAndResponse(routes, req)(Status.Ok, expectedResponse)
  }

  test("return BadRequest and errors response when user was not found") {
    val req                        = GET(uri"/developers/connected/dev1/dev2")
    val expectedResponse           = ErrorsResponse(List("msg"))
    val developerConnectionService = mockedDeveloperConnectionService(IO.pure(Validated.invalidNel(UserNotFound("msg"))))
    val routes                     = DeveloperConnectionRoutes[IO](developerConnectionService).routes

    expectHttpStatusAndResponse(routes, req)(Status.BadRequest, expectedResponse)
  }

  test("return InternalServerError response in case of unhandled exception") {
    val req                        = GET(uri"/developers/connected/dev1/dev2")
    val developerConnectionService = mockedDeveloperConnectionService(IO.raiseError(new RuntimeException("Broken")))
    val routes                     = DeveloperConnectionRoutes[IO](developerConnectionService).routes

    expectHttpStatus(routes, req)(Status.InternalServerError)
  }
}
