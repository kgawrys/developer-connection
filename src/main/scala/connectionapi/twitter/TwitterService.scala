package connectionapi.twitter

import cats.MonadError
import cats.effect.Async
import cats.implicits._
import connectionapi.developerconnection.domain.developerconnection.DeveloperName
import connectionapi.twitter.config.TwitterConfig
import connectionapi.twitter.domain.TwitterDomain.TwitterId
import connectionapi.twitter.domain.TwitterResponse.TwitterException
import connectionapi.twitter.domain.TwitterResponse.TwitterException.{ APICallFailure, UserNotFound }
import connectionapi.twitter.domain.dto.TwitterUserFollowing
import connectionapi.twitter.domain.dto.TwitterUserLookup
import connectionapi.twitter.domain.dto.{ TwitterError, TwitterUserInfo }
import io.circe.Decoder
import org.http4s.Method._
import org.http4s._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.headers.Authorization
import org.typelevel.log4cats.Logger

trait TwitterService[F[_]] {
  def userLookup(developerName: DeveloperName): F[TwitterUserLookup]
  def followingById(twitterId: TwitterId): F[TwitterUserFollowing]
  def followingByDeveloperName(developerName: DeveloperName): F[TwitterUserFollowing]
}

object TwitterService {
  def make[F[_]: Async: Logger](
      client: Client[F],
      config: TwitterConfig
  ): TwitterService[F] =
    new TwitterService[F] with Http4sClientDsl[F] {

      implicit val userLookupEntityDecoder: EntityDecoder[F, TwitterUserInfo] = jsonOf[F, TwitterUserInfo]

      private def userLookupPath(developerName: DeveloperName) = s"${config.baseUri}/users/by/username/${developerName.value}"
      private def userFollowingPath(twitterId: TwitterId)      = s"${config.baseUri}/users/${twitterId.value}/following"

      override def followingByDeveloperName(developerName: DeveloperName): F[TwitterUserFollowing] =
        handleErrors(for {
          twitterId     <- userLookup(developerName)
          userFollowing <- followingById(twitterId.data.id)
        } yield userFollowing)

      override def followingById(twitterId: TwitterId): F[TwitterUserFollowing] =
        handleErrors(for {
          uri      <- buildFollowingUri(twitterId)
          request  <- buildRequest(uri)
          response <- sendRequest[TwitterUserFollowing](client, request, twitterId.value.toString)
          _        <- Logger[F].info(s"Got response: $response")
        } yield response)

      override def userLookup(developerName: DeveloperName): F[TwitterUserLookup] =
        handleErrors(for {
          uri      <- buildLookupUri(developerName)
          request  <- buildRequest(uri)
          response <- sendRequest[TwitterUserLookup](client, request, developerName.value)
          _        <- Logger[F].info(s"Got response: $response")
        } yield response)

      private def handleErrors[A](result: F[A]) =
        result.handleErrorWith {
          case err: TwitterException =>
            Logger[F].error(s"Failure occurred during fetching data from Twitter: $err Message: ${err.message}") *>
              MonadError[F, Throwable].raiseError(err)
          case err =>
            Logger[F].error(s"Unexpected failure occurred during fetching data from Twitter: $err. Message: ${Option(err.getMessage).getOrElse("")}") *>
              MonadError[F, Throwable].raiseError(err)
        }

      private def buildFollowingUri(twitterId: TwitterId): F[Uri] =
        Uri.fromString(userFollowingPath(twitterId)).liftTo[F]

      private def buildLookupUri(developerName: DeveloperName): F[Uri] =
        Uri.fromString(userLookupPath(developerName)).liftTo[F]

      private def buildRequest(uri: Uri): F[Request[F]] =
        Async[F].pure(
          GET.apply(
            uri,
            Authorization(Credentials.Token(AuthScheme.Bearer, config.bearer))
          )
        )

      private def sendRequest[A: Decoder](client: Client[F], request: Request[F], userHandle: String): F[A] =
        Logger[F].info(s"Sending request to Twitter: $request") *>
          client
            .run(request)
            .use(response => handleResponse[A](response, userHandle))

      private def handleResponse[A: Decoder](response: Response[F], userHandle: String): F[A] =
        response.status match {
          case Status.Ok => decodeWithFallback[A](response, userHandle)
          case st        => APICallFailure(buildMsg(st)).raiseError[F, A]
        }

      /** In case of not found user, Twitter returns OK and different message type. It is handled with "fallback" serialization to error type in case when
        * failure in serialization of the main type occurs.
        */
      private def decodeWithFallback[A: Decoder](response: Response[F], userHandle: String): F[A] =
        response
          .asJsonDecode[A]
          .attempt
          .flatMap {
            case Left(_)      => decodeFallback[A](response, userHandle)
            case Right(value) => Async[F].delay(value)
          }

      private def decodeFallback[A](response: Response[F], userHandle: String): F[A] =
        response.asJsonDecode[TwitterError].attempt.flatMap {
          case Left(t)  => MonadError[F, Throwable].raiseError(t)
          case Right(_) => UserNotFound(s"$userHandle is not a valid user in Twitter").raiseError[F, A]

        }

      private def buildMsg(st: Status) = s"Failed with code: ${st.code} and message: ${Option(st.reason).getOrElse("unknown")}"
    }
}
