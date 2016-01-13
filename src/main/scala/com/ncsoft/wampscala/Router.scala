package com.ncsoft.wampscala

import java.util.concurrent.ConcurrentHashMap

import akka.actor._
import play.api.libs.json.{JsObject, Json}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits

object Router {
  val sessions = new ConcurrentHashMap[Long, Session].asScala // session id, session data

  val realmSet = new ConcurrentHashMap[String, String].asScala // set처럼 사용 (realm, realm)

  protected val welcomeDetails = Json.parse(
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
}

class Router(clientOpt:Option[ActorRef] = None,
             pubSubServiceOpt: Option[PubSubService] = None,
             idGeneratorOpt:Option[IdGenerator] = None,
             ec:ExecutionContext = Implicits.global) extends Actor {

  val idGenerator = idGeneratorOpt.getOrElse(new SimpleIdGenerator)

  val broker = new Broker(this, pubSubServiceOpt.getOrElse(new SimplePubSubService(idGenerator, ec)))
  val dealer = new Dealer(this)

  var sessionOpt:Option[Session] = None

  def onHello(msg:Hello, client:ActorRef) = {
    val response:Message = sessionOpt.map { session =>
      Welcome(session.id, Json.obj())
    }.getOrElse {
      try {
        if (!Uri.isValid(msg.realm))
          throw new InvalidUriException(msg.realm)

        val session = Session(idGenerator(IdScope.Global), msg.realm, self, client, new mutable.HashSet[Long]())
        sessionOpt = Some(session)
        Router.sessions.put(session.id, session)
        Router.realmSet.putIfAbsent(session.realm, session.realm)

        Welcome(session.id, Router.welcomeDetails)
      }
      catch {
        case t:WampException =>
          Abort(Json.obj("message" -> t.message), t.reason)
        case t:Throwable =>
          val x = t
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
        sessionOpt = None
        Router.sessions.remove(session.id)

        client ! GoodBye(Json.obj(), Reason.GoodByeAndOut)

        self ! PoisonPill

      case _ =>
      // Do nothing

    }
  }

  def messageHandler(client:ActorRef):PartialFunction[Message, Unit] = {
    case msg: Hello =>        onHello(msg, client)
    case msg: Authenticate => onAuthenticate(msg, client)
    case msg: GoodBye =>      onGoodBye(msg, client)
  }

  def integratedMessageHandler(client:ActorRef) =
    messageHandler(client) orElse
      broker.messageHandler(client) orElse
      dealer.messageHandler(client)

  def receive:Receive = {
    case wampMsg:Message =>
      val client = clientOpt.getOrElse(sender())
      integratedMessageHandler(client)(wampMsg)
  }

  override def postStop(): Unit = {
    sessionOpt.foreach { session =>
      Router.sessions.remove(session.id)

      session.subscriptionIds.foreach { subscriptionId =>
        broker.unsubscribe(session, subscriptionId)
      }
    }



  }
}