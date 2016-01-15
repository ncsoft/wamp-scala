package com.ncsoft.wampscala

import akka.actor.{PoisonPill, ActorSystem}
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
  implicit def akkaTimeToDuration(akkaTimeout:akka.util.Timeout):FiniteDuration = akkaTimeout.duration

  val idGen = new SimpleIdGenerator

  override def beforeAll(): Unit = {
  }

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

  "Dealer" must {
    "handle REGISTER" in {
      val router = new Router()
      val routerActor = TestActorRef(new RouterActor(router))

      routerActor ! Hello("realm", Json.obj())
      expectMsgClass[Welcome](timeout, classOf[Welcome])

      val procedure = "test_procedure"
      routerActor ! Register(idGen(IdScope.Global), Json.obj(), procedure)
      expectMsgClass[Registered](timeout, classOf[Registered])

      routerActor ! PoisonPill
    }

    "handle CALL" in {
      val router = new Router()
      val routerActor = TestActorRef(new RouterActor(router))

      val procedure = Procedure("test_procedure", in => {
        Yield(in.requestId, Json.obj(), None, None)
      })

      routerActor ! Hello("realm", Json.obj())
      expectMsgClass[Welcome](timeout, classOf[Welcome])

      routerActor ! Register(idGen(IdScope.Global), Json.obj(), procedure.name)
      expectMsgClass[Registered](timeout, classOf[Registered])

      val callRequestId = idGen(IdScope.Global)
      routerActor ! Call(callRequestId, Json.obj(), procedure.name, None, None)
      val invocation = expectMsgClass[Invocation](timeout, classOf[Invocation])

      routerActor ! procedure.action(invocation) // yield

      val r = expectMsgClass[Result](timeout, classOf[Result])
      assert(r.requestId == callRequestId)

      routerActor ! PoisonPill
    }

    "handle UNREGISTER" in {
      val router = new Router()
      val routerActor = TestActorRef(new RouterActor(router))

      routerActor ! Hello("realm", Json.obj())
      expectMsgClass[Welcome](timeout, classOf[Welcome])

      val procedure = "test_procedure"

      routerActor ! Register(idGen(IdScope.Global), Json.obj(), procedure)
      val r1 = expectMsgClass[Registered](timeout, classOf[Registered])

      routerActor ! Unregister(idGen(IdScope.Global), r1.registrationId)
      expectMsgClass[Unregistered](timeout, classOf[Unregistered])

      routerActor ! PoisonPill
    }

    "unregister when session is closed" in {
      val router = new Router()
      val routerActor = TestActorRef(new RouterActor(router))

      routerActor ! Hello("realm", Json.obj())
      expectMsgClass[Welcome](timeout, classOf[Welcome])

      val procedure = "test_procedure"

      val f1 = routerActor ? Register(idGen(IdScope.Global), Json.obj(), procedure)
      val Success(r1:Message) = f1.value.get
      assert(r1.code == MessageCode.REGISTERED)
      val registrationId = r1.asInstanceOf[Registered].registrationId

      routerActor ! GoodBye(Json.obj(), "")
      expectMsgClass[GoodBye](timeout, classOf[GoodBye])

      assert(router.registrationsById.get(registrationId).isEmpty)

      routerActor ! PoisonPill

    }
  }
}