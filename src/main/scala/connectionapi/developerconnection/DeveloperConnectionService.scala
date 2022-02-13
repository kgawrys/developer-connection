package connectionapi.developerconnection

import cats.effect.Async
import cats.effect._
import cats.implicits._
import connectionapi.developerconnection.domain.dto.DeveloperConnectionResponse
import connectionapi.github.GithubApiService
import connectionapi.developerconnection.domain.developerconnection.DeveloperName
import org.typelevel.log4cats.Logger

trait DeveloperConnectionService[F[_]] {
  def areConnected(devName1: DeveloperName, devName2: DeveloperName): F[DeveloperConnectionResponse]
}

object DeveloperConnectionService {

  def make[F[_]: Async: Logger](
      githubApiService: GithubApiService[F]
  ): DeveloperConnectionService[F] =
    new DeveloperConnectionService[F] {
      def areConnected(devName1: DeveloperName, devName2: DeveloperName): F[DeveloperConnectionResponse] =
        Logger[F].debug(s"devName1: ${devName1.value}, devName2: ${devName2.value}") *> // todo remove this logger
          Async[F].pure(DeveloperConnectionResponse(false, Nil)) // todo to implement
    }

}
