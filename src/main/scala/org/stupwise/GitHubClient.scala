package org.stupwise

import cats.effect.Sync
import cats.implicits._
import AppConfig.ClientConfig
import org.http4s._
import org.http4s.client.Client
import org.http4s.headers._

import scala.annotation.tailrec


trait GitHubClient[F[_]] {
  def get[A](query: String)(implicit d: EntityDecoder[F, A]): F[A]
  def getAll[A](query: String)(implicit d: EntityDecoder[F, List[A]]): F[List[A]]
}

object GitHubClient {

  def apply[F[_]: Sync](
    httpClient: Client[F],
    clientConfig: ClientConfig
  ): GitHubClient[F] = new GitHubClient[F] {
    import clientConfig._

    override def getAll[A](query: String)(implicit d: EntityDecoder[F, List[A]]): F[List[A]] = {

      def readAllPages(page: Int)(implicit d: EntityDecoder[F, List[A]]): F[List[A]] = {
        val iter = expect(query, page)

        iter.flatMap { request =>
          if(request.size < perPage) {
            Sync[F].pure(request)
          } else {
            readAllPages(page + 1).map(_ ++ request)
          }
        }
      }

      readAllPages(0)
    }

    override def get[A](query: String)(implicit d: EntityDecoder[F, A]): F[A] = {
      expect(query)
    }

    private def expect[A](query: String, page: Int = 0)(implicit d: EntityDecoder[F, A]): F[A] = {
      httpClient.expect(getRequest(query, page))
    }

    private def getRequest(query: String, page: Int): Request[F] = {
      val uri = baseUri.addPath(query) =? Map("page" -> List(page), "per_page" -> List(perPage))
      Request[F](uri = uri).putHeaders(Authorization(Credentials.Token(AuthScheme.Bearer, token)))
    }
  }
}
