package connectionapi.utils

import cats.effect.IO
import io.circe.Decoder
import org.http4s.circe._
import org.http4s.{ HttpRoutes, Request, Status }
import weaver.Expectations

trait HttpTestUtils extends Expectations.Helpers {

  def expectHttpStatus(routes: HttpRoutes[IO], req: Request[IO])(expectedStatus: Status): IO[Expectations] =
    routes.run(req).value.map {
      case Some(resp) => expect.same(expectedStatus, resp.status)
      case None       => failure("Not found route")
    }

  def expectHttpStatusAndResponse[Resp: Decoder](
      routes: HttpRoutes[IO],
      req: Request[IO]
  )(expectedStatus: Status, expectedResponse: Resp): IO[Expectations] =
    routes.run(req).value.flatMap {
      case Some(resp) =>
        resp
          .asJsonDecode[Resp]
          .map { response =>
            expect.same(expectedStatus, resp.status) and expect.same(expectedResponse, response)
          }
      case None => IO.pure(failure("Not found route"))
    }
}
