package connectionapi.github

import cats.MonadError
import cats.effect.Async
import cats.implicits._
import connectionapi.developerconnection.domain.developerconnection.DeveloperName
import connectionapi.github.config.GithubConfig
import connectionapi.github.domain.GithubResponse.GithubException
import connectionapi.github.domain.GithubResponse.GithubException.{ APICallFailure, UserNotFound }
import connectionapi.github.domain.dto.GithubOrganization
import org.http4s.Method._
import org.http4s._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.headers.{ Accept, `User-Agent` }
import org.typelevel.log4cats.Logger

trait GithubService[F[_]] {
  def getOrganizations(developerName: DeveloperName): F[Seq[GithubOrganization]]
}

object GithubService {
  def make[F[_]: Async: Logger](
      client: Client[F],
      config: GithubConfig
  ): GithubService[F] =
    new GithubService[F] with Http4sClientDsl[F] {

      implicit val organizationsEntityDecoder: EntityDecoder[F, Seq[GithubOrganization]] = jsonOf[F, Seq[GithubOrganization]]

      private def userOrganizationsPath(developerName: DeveloperName) = s"${config.baseUri}/users/${developerName.value}/orgs"

      override def getOrganizations(developerName: DeveloperName): F[Seq[GithubOrganization]] = {
        val result = for {
          uri      <- buildUri(developerName)
          request  <- buildRequest(uri)
          response <- sendRequest(client, request, developerName)
        } yield response

        result.handleErrorWith {
          case err: GithubException =>
            Logger[F].error(s"Failure occurred during user organizations fetching: $err Message: ${err.message}") *>
              MonadError[F, Throwable].raiseError(err)
          case err =>
            Logger[F].error(s"Unexpected failure occurred during user organizations fetching: $err Message: ${Option(err.getMessage).getOrElse("")}") *>
              MonadError[F, Throwable].raiseError(err)
        }
      }

      private def buildUri(developerName: DeveloperName): F[Uri] =
        Uri.fromString(userOrganizationsPath(developerName)).liftTo[F]

      /** @Accept
        *   header is recommended by Github.
        * @User-Agent
        *   header is required by Github.
        *
        * More details in above link. https://docs.github.com/en/rest/overview/resources-in-the-rest-api
        */
      private def buildRequest(uri: Uri): F[Request[F]] =
        Async[F].pure(
          GET.apply(
            uri,
            Accept.parse("application/vnd.github.v3+json"),
            `User-Agent`.parse("developer-connection")
          )
        )

      private def sendRequest(client: Client[F], request: Request[F], developerName: DeveloperName): F[Seq[GithubOrganization]] =
        Logger[F].info(s"Sending request to Github: $request") *>
          client
            .run(request)
            .use(response => handleResponse(response, developerName))

      private def handleResponse(response: Response[F], developerName: DeveloperName): F[Seq[GithubOrganization]] =
        response.status match {
          case Status.Ok => response.asJsonDecode[Seq[GithubOrganization]]
          case Status.NotFound =>
            UserNotFound(s"${developerName.value} is no a valid user in Github")
              .raiseError[F, Seq[GithubOrganization]] // todo UserNotFound is not an error, could be passed as an either
          case st => APICallFailure(buildMsg(st)).raiseError[F, Seq[GithubOrganization]]
        }

      private def buildMsg(st: Status) = s"Failed with code: ${st.code} and message: ${Option(st.reason).getOrElse("unknown")}"
    }

}
