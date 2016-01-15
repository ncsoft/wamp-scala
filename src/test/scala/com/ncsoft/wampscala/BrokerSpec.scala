package com.ncsoft.wampscala

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import org.scalatest._
import play.api.libs.json.Json

import scala.concurrent.duration._
import scala.util.Success

class BrokerSpec(_system:ActorSystem) extends TestKit(_system) with ImplicitSender with WordSpecLike with Matchers
with BeforeAndAfterAll {
  def this() = this(ActorSystem("BrokerSpec"))

  implicit val timeout:akka.util.Timeout = 10.second

  implicit def akkaTimeToDuration(akkaTimeout:akka.util.Timeout):FiniteDuration = akkaTimeout.duration

  override def beforeAll(): Unit = {
  }

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  val idGen = new SimpleIdGenerator

  "Broker" must {

    "handle SUBSCRIBE" in {
      val router = new Router()
      val routerActor = TestActorRef(new RouterActor(router))

      val futureResult = routerActor ? Hello("realm", Json.obj())
      val Success(result:Message) = futureResult.value.get

      assert(result.code == MessageCode.WELCOME)

      val topicString = "test_topic"
      val f = routerActor ? Subscribe(idGen(IdScope.Session), Json.obj(), topicString)
      val Success(r:Message) = f.value.get

      assert(r.code == MessageCode.SUBSCRIBED)
    }

    "handle UNSUBSCRIBE" in {
      val router = new Router()
      val routerActor = TestActorRef(new RouterActor(router))

      val futureResult = routerActor ? Hello("realm", Json.obj())
      val Success(result:Message) = futureResult.value.get

      assert(result.code == MessageCode.WELCOME)

      val topicString = "test_topic"
      val f = routerActor ? Subscribe(idGen(IdScope.Session), Json.obj(), topicString)
      val Success(r:Message) = f.value.get

      assert(r.code == MessageCode.SUBSCRIBED)

      val subscriptionId = r.asInstanceOf[Subscribed].subscriptionId
      val f2 = routerActor ? Unsubscribe(idGen(IdScope.Session), subscriptionId)
      val Success(r2:Message) = f2.value.get

      assert(r2.code == MessageCode.UNSUBSCRIBED)
    }

    "handle PUBLISH" in {
      val router = new Router()
      val routerActor = TestActorRef(new RouterActor(router))

      routerActor ! Hello("realm", Json.obj())
      expectMsgClass[Welcome](timeout, classOf[Welcome])

      val topicString = "test_topic"

      routerActor ! Subscribe(idGen(IdScope.Session), Json.obj(), topicString)
      expectMsgClass[Subscribed](timeout, classOf[Subscribed])

      val argKw = Json.obj("name" -> "test")
      routerActor ! Publish(idGen(IdScope.Session), Json.obj(), topicString, None, Some(argKw))
      expectMsgClass[Published](timeout, classOf[Published])

      val m = expectMsgClass[Event](timeout, classOf[Event])

      assert( (m.publishArgumentsKwOpt.get \ "name").as[String] == "test" )
    }

  }
}