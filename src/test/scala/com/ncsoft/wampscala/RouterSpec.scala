package com.ncsoft.wampscala

import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestKit, TestActorRef}
import akka.actor.ActorSystem

import org.scalatest._
import spray.json.{JsObject, JsNumber}

import scala.util.Success
import scala.concurrent.duration._

class RouterSpec(_system:ActorSystem) extends TestKit(_system) with ImplicitSender with WordSpecLike with Matchers
with BeforeAndAfterAll {
  def this() = this(ActorSystem("RouterSpec"))

  implicit val timeout:akka.util.Timeout = 10.second

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "Router" must {
    "handle HELLO" in {
      val routerActor = TestActorRef(new Router(new SimplePubSubService))
      val futureResult = routerActor ? Hello("realm", JsObject())
      val Success(result:Message) = futureResult.value.get

      assert(result.code == MessageCode.WELCOME)
    }


  }
}