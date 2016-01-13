package com.ncsoft.wampscala

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestActors}
import org.scalatest._
import play.api.libs.json.Json

import scala.concurrent.duration._
import scala.util.Success

class RouterSpec(_system:ActorSystem) extends TestKit(_system) with ImplicitSender with WordSpecLike with Matchers
with BeforeAndAfterAll {
  def this() = this(ActorSystem("RouterSpec"))

  implicit val timeout:akka.util.Timeout = 10.second

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  val idGen = new SimpleIdGenerator

  "Router" must {
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

    "handle SUBSCRIBE" in {
      val router = TestActorRef(new Router)

      val futureResult = router ? Hello("realm", Json.obj())
      val Success(result:Message) = futureResult.value.get

      assert(result.code == MessageCode.WELCOME)

      val topicString = "test_topic"
      val f = router ? Subscribe(idGen(IdScope.Session), Json.obj(), topicString)
      val Success(r:Message) = f.value.get

      assert(r.code == MessageCode.SUBSCRIBED)
    }

    "handle UNSUBSCRIBE" in {
      val router = TestActorRef(new Router)

      val futureResult = router ? Hello("realm", Json.obj())
      val Success(result:Message) = futureResult.value.get

      assert(result.code == MessageCode.WELCOME)

      val topicString = "test_topic"
      val f = router ? Subscribe(idGen(IdScope.Session), Json.obj(), topicString)
      val Success(r:Message) = f.value.get

      assert(r.code == MessageCode.SUBSCRIBED)

      val subscriptionId = r.asInstanceOf[Subscribed].subscriptionId
      val f2 = router ? Unsubscribe(idGen(IdScope.Session), subscriptionId)
      val Success(r2:Message) = f2.value.get
      val s = router.underlyingActor.pubSubService.asInstanceOf[SimplePubSubService]

      assert(r2.code == MessageCode.UNSUBSCRIBED)
      assert(!s.subscribedTopics.contains(subscriptionId))
    }

    "handle PUBLISH" in {
      val router = TestActorRef(new Router)

      router ! Hello("realm", Json.obj())
      expectMsgClass[Welcome](3.second, classOf[Welcome])

      val topicString = "test_topic"

      router ! Subscribe(idGen(IdScope.Session), Json.obj(), topicString)
      expectMsgClass[Subscribed](3.second, classOf[Subscribed])

      val argKw = Json.obj("name" -> "test")
      router ! Publish(idGen(IdScope.Session), Json.obj(), topicString, None, Some(argKw))
      expectMsgClass[Published](3.second, classOf[Published])

      val m = expectMsgClass[Event](3.second, classOf[Event])

      assert( (m.publishArgumentsKwOpt.get \ "name").as[String] == "test" )

    }

  }
}