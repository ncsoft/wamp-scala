package com.ncsoft.wampscala

import java.util.concurrent.ConcurrentHashMap

import akka.actor._
import akka.routing.RoundRobinPool
import play.api.libs.json.{JsObject, Json}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits

class Router(
              idGeneratorOpt:Option[IdGenerator] = None,
              pubSubServiceOpt: Option[PubSubService] = None,
              ecOpt:Option[ExecutionContext] = None,
              numberOfInstance:Int = 5
              ) {

  val sessions = new ConcurrentHashMap[Long, Session].asScala // session id, session data
  val realmSet = new ConcurrentHashMap[String, String].asScala // set처럼 사용 (realm, realm)

  val registrationsById = new mutable.HashMap[Long, Registration]
  val registrationsByProcedure = new mutable.HashMap[String, Registration]
  val invokeContexts = new mutable.HashMap[Long, InvokeContext]

  val welcomeDetails = Json.parse(
    """
      |{
      |  "roles": {
      |    "broker": {
      |      "features": {
      |        "subscriber_blackwhite_listing"  : true,
      |        "publisher_exclusion"            : true,
      |        "publisher_identification"       : true,
      |        "publication_trustlevels"        : true
      |      }
      |    },
      |    "dealer": {
      |      "features": {
      |        "callee_blackwhite_listing"      : false,
      |        "caller_exclusion"               : false,
      |        "caller_identification"          : true,
      |        "call_trustlevels"               : true
      |      }
      |    }
      |  }
      |}
    """.stripMargin
  ).as[JsObject]

  val idGenerator = idGeneratorOpt.getOrElse(new SimpleIdGenerator)
  val pubSubService = pubSubServiceOpt.getOrElse(new SimplePubSubService(idGenerator, ecOpt.getOrElse(Implicits.global)))
//  val router = actorSystemOpt.getOrElse(
//    ActorSystem("wamp-scala")
//  ).actorOf(RoundRobinPool(numberOfInstance).props(Props(classOf[RouterActor], this)), "router")

  val predefinedProcedures = Map(

  )

  def createActor(context:ActorContext):ActorRef = {
    context.actorOf(Props(new RouterActor(this)))
  }

  protected def registerPredefinedProcedures(): Unit = {
    // TODO: implement
  }
}

class RouterActor(router:Router) extends Actor {
  def idGenerator = router.idGenerator

  val broker = new Broker(router, self)
  val dealer = new Dealer(router, self)

  var sessionOpt:Option[Session] = None

  def receive:Receive = {
    case wampMsg:Message =>
      val client = sender()
      integratedMessageHandler(client)(wampMsg)
  }

  def integratedMessageHandler(client:ActorRef) =
    messageHandler(client) orElse
      broker.messageHandler(sessionOpt, client) orElse
      dealer.messageHandler(sessionOpt, client)

  def messageHandler(client:ActorRef):PartialFunction[Message, Unit] = {
    case msg: Hello =>        onHello(msg, client)
    case msg: Authenticate => onAuthenticate(msg, client)
    case msg: GoodBye =>      onGoodBye(msg, client)
  }

  def onHello(msg:Hello, client:ActorRef) = {
    val response = sessionOpt.map { session =>
      Welcome(session.id, Json.obj())
    }.getOrElse {
      try {
        if (!Uri.isValid(msg.realm))
          throw new InvalidUriException(msg.realm)

        val session = Session(
          idGenerator(IdScope.Global), msg.realm, self, client,
          new mutable.HashSet[Long], new mutable.HashSet[Long]
        )

        sessionOpt = Some(session)
        router.sessions.put(session.id, session)
        router.realmSet.putIfAbsent(session.realm, session.realm)

        Welcome(session.id, router.welcomeDetails)
      }
      catch {
        case t:WampException =>
          Abort(Json.obj("message" -> t.message), t.reason)
        case t:Throwable =>
          Abort(Json.obj("message" -> t.getMessage), Reason.Unknown)
      }
    }

    client ! response

    if (response.isInstanceOf[Abort])
      self ! PoisonPill
  }

  def onAuthenticate(msg:Authenticate, client:ActorRef): Unit = {
    client ! Error(MessageCode.INVALID, idGenerator(IdScope.Session), Json.obj(), "Not implemented", None, None)
  }

  def onGoodBye(msg:GoodBye, client:ActorRef): Unit = {
    sessionOpt match {
      case Some(session) =>
        client ! GoodBye(Json.obj(), Reason.GoodByeAndOut)

        self ! PoisonPill

      case _ =>
      // Do nothing

    }
  }

  protected def closeSession() {
    sessionOpt.foreach { session =>
      router.sessions.remove(session.id)

      session.subscriptionIds.foreach { subscriptionId =>
        broker.unsubscribe(session, subscriptionId)
      }

      session.registrationIds.foreach { registrationId =>
        dealer.unregister(registrationId)
      }
    }

    sessionOpt = None
  }

  override def postStop(): Unit = {
    closeSession()
  }
}