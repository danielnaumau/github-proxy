package org.stupwise

import cats.Parallel
import cats.effect.Async
import AppConfig.HttpConfig
import org.http4s.implicits._
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router

final class HttpServer[F[_]: Async](routes: HttpRoutes[F]) {
  def start(config: HttpConfig): F[Unit] = {
    BlazeServerBuilder[F]
      .bindHttp(config.port, config.host)
      .withHttpApp(routes.orNotFound)
      .serve
      .compile
      .drain
  }
}

object HttpServer {
  def apply[F[_]: Async: Parallel](client: GitHubClient[F]): HttpServer[F] = {
    val organizationService = new OrganizationService(client)

    new HttpServer(
      Router[F](
        "/org/" -> organizationService.routes,
      )
    )
  }
}
