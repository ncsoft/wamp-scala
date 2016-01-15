package com.ncsoft.wampscala

import akka.actor.{Props, ActorSystem}
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import org.scalatest._
import play.api.libs.json.Json

import scala.concurrent.duration._
import scala.util.Success

class RouterSpec(_system:ActorSystem) extends TestKit(_system) with ImplicitSender with WordSpecLike with Matchers
with BeforeAndAfterAll {
  def this() = this(ActorSystem("RouterSpec"))

  implicit val timeout:akka.util.Timeout = 10.second

  override def beforeAll(): Unit = {

  }

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "Router" must {
    "handle HELLO" in {
      val router = new Router()
      val routerActor = TestActorRef(new RouterActor(router))

      val futureResult = routerActor ? Hello("realm", Json.obj())
      val Success(result:Message) = futureResult.value.get

      assert(result.code == MessageCode.WELCOME)
    }

    "handle GOODBYE" in {
      val router = new Router()
      val routerActor = TestActorRef(new RouterActor(router))

      val futureResult = routerActor ? Hello("realm", Json.obj())
      val Success(result:Message) = futureResult.value.get

      assert(result.code == MessageCode.WELCOME)

      val futureResult2 = routerActor ? GoodBye(Json.obj(), Reason.GoodByeAndOut)
      val Success(result2:Message) = futureResult2.value.get

      assert(result2.code == MessageCode.GOODBYE)
    }
  }
}