package connectionapi

import cats.effect._
import connectionapi.config.Config
import connectionapi.developerconnection.DeveloperConnectionService
import connectionapi.github.GithubApiService
import connectionapi.routes.DeveloperConnectionRoutes
import fs2.Stream
import org.http4s.HttpApp
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.{ RequestLogger, ResponseLogger }
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Server {

  def serve(config: Config): Stream[IO, ExitCode] = {

    implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

    lazy val httpClient: Resource[IO, Client[IO]] = EmberClientBuilder
      .default[IO]
      .withTimeout(config.httpClient.timeout)
      .withIdleTimeInPool(config.httpClient.idleTimeInPool)
      .build

    val routes: Resource[IO, HttpApp[IO]] = for {
      client <- httpClient
    } yield {
      val githubApiService           = GithubApiService.make[IO](client, config.githubConfig)
      val developerConnectionService = DeveloperConnectionService.make(githubApiService)

      DeveloperConnectionRoutes[IO](developerConnectionService).routes.orNotFound
    }

    val loggers: HttpApp[IO] => HttpApp[IO] = {
      { http: HttpApp[IO] =>
        RequestLogger.httpApp(true, true)(http)
      } andThen { http: HttpApp[IO] =>
        ResponseLogger.httpApp(true, true)(http)
      }
    }

    Stream
      .resource(routes)
      .evalMap { routes =>
        EmberServerBuilder
          .default[IO]
          .withHost(config.app.host)
          .withPort(config.app.port)
          .withHttpApp(loggers(routes))
          .build
          .useForever
      }
  }

}
