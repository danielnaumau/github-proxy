package com.scalac

object GitHubMsg {
  object In {
    case class Repository(name: String)
    case class Contributor(login: String, contributions: Int)
  }

  object Out {
    case class ContributorResult(name: String, contributions: Int)
  }
}
