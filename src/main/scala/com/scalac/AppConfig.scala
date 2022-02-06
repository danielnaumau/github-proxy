package com.scalac

import cats.effect.Sync
import com.scalac.AppConfig._
import org.http4s.Uri
import pureconfig.{ConfigReader, ConfigSource}
import pureconfig.generic.auto._

final case class AppConfig(
  http: HttpConfig,
  client: ClientConfig
)

object AppConfig {
  final case class HttpConfig(host: String, port: Int)
  final case class ClientConfig(baseUri: Uri, token: String, perPage: Int)

  private implicit val UriReader: ConfigReader[Uri] = ConfigReader.fromStringTry(Uri.fromString(_).toTry)

  def load[F[_]: Sync]: F[AppConfig] = Sync[F].delay(ConfigSource.default.loadOrThrow[AppConfig])
}
