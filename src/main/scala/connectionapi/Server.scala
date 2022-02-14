package connectionapi

import cats.effect._
import connectionapi.config.Config
import connectionapi.developerconnection.DeveloperConnectionService

import connectionapi.github.GithubService
import connectionapi.routes.DeveloperConnectionRoutes
import connectionapi.twitter.TwitterService
import fs2.Stream
import org.http4s.HttpApp
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.client.Client
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.{ RequestLogger, ResponseLogger }
import org.typelevel.log4cats.SelfAwareStructuredLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object Server {

  def serve(config: Config): Stream[IO, ExitCode] = {

    implicit val logger: SelfAwareStructuredLogger[IO] = Slf4jLogger.getLogger[IO]

    lazy val httpClient: Resource[IO, Client[IO]] = BlazeClientBuilder[IO].withDefaultSslContext.resource

    val routes: Resource[IO, HttpApp[IO]] = for {
      client <- httpClient
    } yield {
      val githubService              = GithubService.make[IO](client, config.githubConfig)
      val twitterService             = TwitterService.make[IO](client, config.twitterConfig)
      val developerConnectionService = DeveloperConnectionService.make(githubService, twitterService)

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
