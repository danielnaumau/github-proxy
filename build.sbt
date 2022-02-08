enablePlugins(JavaAppPackaging)

name := "github-proxy"

version := git.gitHeadCommit.value.getOrElse("0.1").take(7)
scalaVersion := "2.13.8"

dockerRepository := Some("danielnaumau")

val http4sVersion = "0.23.10"
val circeVersion  = "0.14.1"
val catsEffect    = "3.3.5"
val scalaTest     = "3.2.10"
val pureconfig    = "0.17.1"

libraryDependencies ++= Seq(
  "org.typelevel"         %% "cats-effect"         % catsEffect,
  "org.http4s"            %% "http4s-dsl"          % http4sVersion,
  "org.http4s"            %% "http4s-blaze-server" % http4sVersion,
  "org.http4s"            %% "http4s-blaze-client" % http4sVersion,
  "org.http4s"            %% "http4s-circe"        % http4sVersion,
  "io.circe"              %% "circe-core"          % circeVersion,
  "io.circe"              %% "circe-generic"       % circeVersion,
  "com.github.pureconfig" %% "pureconfig"          % pureconfig,
  "org.scalatest"         %% "scalatest"           % scalaTest
)
