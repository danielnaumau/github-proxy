package com.scalac

import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.blaze.client.BlazeClientBuilder

object Main extends IOApp {

  private val httpClient = BlazeClientBuilder[IO].resource

  override def run(args: List[String]): IO[ExitCode] = {
    httpClient.use { client =>
      for {
        config      <- AppConfig.load[IO]
        githubClient = GitHubClient(client, config.client)
        _           <- HttpServer(githubClient).start(config.http)
      } yield ExitCode.Success
    }
  }
}
