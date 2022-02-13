package connectionapi.routes

import cats.effect.Sync
import cats.implicits._
import connectionapi.developerconnection.DeveloperConnectionService
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import connectionapi.routes.http.PathVariables._
import org.typelevel.log4cats.Logger

final case class DeveloperConnectionRoutes[F[_]: Sync: Logger](developerConnectionService: DeveloperConnectionService[F]) extends Http4sDsl[F] {

  val prefix = "/developers/connected"

  private val get: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root / DeveloperNameVar(devName1) / DeveloperNameVar(devName2) =>
      developerConnectionService
        .areConnected(devName1, devName2)
        .flatMap(Ok(_)) // todo add error handling and adt for errors
        .handleErrorWith { ex =>
          Logger[F].error(s"Unhandled error occurred: $ex")
          InternalServerError()
        }
  }

  def routes: HttpRoutes[F] =
    Router(
      prefix -> get
    )
}
