package it

import org.scalatest.{BeforeAndAfter, EitherValues}
import org.scalatest.flatspec.AnyFlatSpec
import org.http4s._

class GitHubProxySpec extends AnyFlatSpec with BeforeAndAfter with EitherValues with GithubProxyFixture {

  it should "return result with sorted contributors if organization exists" in {
    val result = sendRequest("api3dao").unsafeRunSync()

    assert(result.value.nonEmpty && isSorted(result.value.map(_.contributions)))
  }

  it should "return status not found if organization doesn't exist" in {
    val result = sendRequest("notFoundOrg").unsafeRunSync()

    assert(result.left.value == Status.NotFound)
  }
}
