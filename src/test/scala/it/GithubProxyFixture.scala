package it

import cats.implicits._
import cats.effect._
import cats.effect.unsafe.IORuntime
import org.stupwise.GitHubMsg.Out.ContributorResult
import org.http4s.{EntityDecoder, Status, Uri}
import io.circe.generic.auto._
import org.http4s.Status.Successful
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.circe._
import org.http4s.dsl.io.GET
import org.http4s.client.dsl.io._

trait GithubProxyFixture {
  implicit val runtime = IORuntime.global

  private implicit val resultDecoder: EntityDecoder[IO, List[ContributorResult]] =
    jsonOf[IO, List[ContributorResult]]


  val (httpClient, _) = BlazeClientBuilder[IO]
    .withCheckEndpointAuthentication(false)
    .resource.allocated.unsafeRunSync()


  private def get(orgName: String) = GET(
    Uri.unsafeFromString(f"http://localhost:8080/org/$orgName/contributors")
  )

  def isSorted(list: List[Int]): Boolean = {
    list == list.sorted
  }

  def sendRequest(orgName: String): IO[Either[Status, List[ContributorResult]]] = {
    httpClient.run(get(orgName)).use {
      case Successful(response) =>
        EntityDecoder[IO, List[ContributorResult]]
          .decode(response, strict = false)
          .value
          .flatMap(_.liftTo[IO])
          .map(_.asRight)
      case response => response.status.asLeft[List[ContributorResult]].pure[IO]
    }
  }
}
