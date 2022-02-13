package connectionapi.routes

import cats.effect.Sync
import cats.implicits._
import connectionapi.developerconnection.DeveloperConnectionService
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router

final case class DeveloperConnectionRoutes[F[_]: Sync](developerConnectionService: DeveloperConnectionService[F]) extends Http4sDsl[F] {

  val prefix = "/developers/connected"

  private val get: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root =>
      developerConnectionService.areConnected
        .flatMap(Ok(_)) // todo add error handling
  }

  def routes: HttpRoutes[F] =
    Router(
      prefix -> get
    )
}
