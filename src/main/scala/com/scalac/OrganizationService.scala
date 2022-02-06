package com.scalac

import cats.Applicative
import cats.effect.Sync
import cats.effect.kernel.Concurrent
import cats.implicits._
import com.scalac.GitHubMsg.In._
import com.scalac.GitHubMsg.Out._
import io.circe.generic.auto._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}


class OrganizationService[F[_]: Applicative: Concurrent](client: GitHubClient[F]) extends Http4sDsl[F] {
  implicit val repositoryDecoder: EntityDecoder[F, List[Repository]] = jsonOf[F, List[Repository]]
  implicit val contributorDecoder: EntityDecoder[F, List[Contributor]] = jsonOf[F, List[Contributor]]

  implicit val resultsEncoder: EntityEncoder[F, List[ContributorResult]] =
    jsonEncoderOf[F, List[ContributorResult]]

  private def getRepositories(orgName: String): F[List[Repository]] = {
    client.getAll[Repository](f"/orgs/$orgName/repos")
  }

  private def getContributors(orgName: String, repName: String): F[List[Contributor]] = {
    client.getAll[Contributor](f"/repos/$orgName/$repName/contributors")
  }

  private def getAllContributors(orgName: String): F[List[ContributorResult]] = {
    for {
      repositories <- getRepositories(orgName)
      contributors <- repositories.flatTraverse(rep => getContributors(orgName, rep.name))
    } yield contributors
      .groupMap(_.login)(_.contributions)
      .map { case (key, value) => ContributorResult(key, value.sum) }
      .toList
      .sortBy(_.contributions)
  }

  val routes: HttpRoutes[F] = HttpRoutes.of {
    case GET -> Root / orgName / "contributors" =>
      getAllContributors(orgName).attempt.flatMap {
        case Left(_)       => NotFound(f"Organization $orgName doesn't exist")
        case Right(result) => Ok(result)
      }
  }
}

object OrganizationService {

  def apply[F[_]: Applicative: Concurrent](client: GitHubClient[F]): OrganizationService[F] = {
    new OrganizationService[F](client)
  }
}