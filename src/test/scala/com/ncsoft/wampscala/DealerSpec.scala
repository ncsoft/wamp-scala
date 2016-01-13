package com.ncsoft.wampscala

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import org.scalatest._
import play.api.libs.json.Json

import scala.concurrent.duration._
import scala.util.Success

class DealerSpec(_system:ActorSystem) extends TestKit(_system) with ImplicitSender with WordSpecLike with Matchers
with BeforeAndAfterAll {
  def this() = this(ActorSystem("DealerSpec"))

  implicit val timeout:akka.util.Timeout = 10.second

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "Dealer" must {
    "handle HELLO" in {
      val router = TestActorRef(new Router)

      val futureResult = router ? Hello("realm", Json.obj())
      val Success(result:Message) = futureResult.value.get

      assert(result.code == MessageCode.WELCOME)
    }

    "handle GOODBYE" in {
      val router = TestActorRef(new Router)

      val futureResult = router ? Hello("realm", Json.obj())
      val Success(result:Message) = futureResult.value.get

      assert(result.code == MessageCode.WELCOME)

      val futureResult2 = router ? GoodBye(Json.obj(), Reason.GoodByeAndOut)
      val Success(result2:Message) = futureResult2.value.get

      assert(result2.code == MessageCode.GOODBYE)
    }
  }
}