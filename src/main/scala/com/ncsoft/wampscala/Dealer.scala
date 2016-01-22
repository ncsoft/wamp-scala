package com.ncsoft.wampscala

import akka.actor.ActorRef
import com.ncsoft.wampscala.MessageCode._
import play.api.libs.json.{JsBoolean, JsNumber, JsObject, Json}

case class Registration(id:Long, procedureName:String, callee:ActorRef)
case class InvokeContext(invocationId:Long, caller:ActorRef, requestId:Long)

class Dealer(router:Router, implicit val routerActor:ActorRef) {
  object TrustLevel {
    val Lowest = 0
    val Low = 1
    val Mid = 2
    val High = 3
    val Default = Lowest
  }

  def idGenerator = router.idGenerator

  def messageHandler(sessionOpt:Option[Session], peer:ActorRef):PartialFunction[Message, Unit] = {
    case msg: Call =>       onCall(msg, sessionOpt,  peer)
    case msg: Yield =>      onYield(msg, sessionOpt, peer)
    case msg: Register =>   onRegister(msg, sessionOpt, peer)
    case msg: Unregister => onUnregister(msg, sessionOpt, peer)
    case msg: Error =>      onError(msg, sessionOpt, peer)
  }

  protected def toRealmUniqueProcedure(realm:String, procedureName:String):String = {
    s"$realm#$procedureName"
  }

  protected def translateCallOptions(options:JsObject, session:Session):JsObject = {

    val translated = options.value.map { case (k, v) =>
      (k, v) match {
        case (Keyword.discloseMe, JsBoolean(true)) =>
          (Keyword.caller, JsNumber(session.id))
        case _ =>
          (k, v)
      }
    } ++ Map(Keyword.trustLevel -> JsNumber(TrustLevel.Lowest))

    JsObject(translated.toSeq)
  }

  def onCall(msg:Call, sessionOpt:Option[Session], caller:ActorRef): Unit = {
    sessionOpt match {
      case Some(session) =>
        if (!Uri.isValid(msg.procedure))
          caller ! Error(CALL, msg.requestId, Json.obj(), Reason.InvalidUri, None, None)

        router.registrationsByProcedure.get(toRealmUniqueProcedure(session.realm, msg.procedure)) match {
          case None =>
             caller ! Error(CALL, msg.requestId, Json.obj(), Reason.NoSuchProcedure, None, None)

          case Some(registration) =>
            val invokeId = idGenerator(IdScope.Session)
            router.invokeContexts.put(invokeId, InvokeContext(invokeId, caller, msg.requestId))

            val invocation = Invocation(invokeId, registration.id, translateCallOptions(msg.options, session),
              msg.argumentsOpt, msg.argumentsKwOpt)

            registration.callee ! invocation
        }

      case _ =>
        caller ! Error(CALL, msg.requestId, Json.obj(), Reason.NotAuthorized, None, None)
    }
  }

  def onYield(msg:Yield, sessionOpt:Option[Session], callee:ActorRef) = {
    router.invokeContexts.remove(msg.requestId) match {
      case Some(context) =>
        context.caller ! Result(context.requestId, msg.options, msg.argumentsOpt, msg.argumentsKwOpt)

      case _ =>
      callee ! Error(CALL, msg.requestId, Json.obj("message" -> "Caller not found"), Reason.Unknown, None, None)
    }
  }

  def onRegister(msg:Register, sessionOpt:Option[Session], callee:ActorRef): Unit = {
    val response = sessionOpt match {
      case Some(session) =>
        val procedureName = toRealmUniqueProcedure(session.realm, msg.procedure)

        router.registrationsByProcedure.isDefinedAt(procedureName) match {
          case true =>
            Error(REGISTER, msg.requestId, Json.obj(), Reason.ProcedureAlreadyExists, None, None)

          case false =>
            val r = Registration(idGenerator(IdScope.Router), procedureName, callee)
            register(session, r)
            Registered(msg.requestId, r.id)
        }

      case _ =>
        Error(REGISTER, msg.requestId, Json.obj(), Reason.NotAuthorized, None, None)
    }

    callee ! response
  }

  def onUnregister(msg:Unregister, sessionOpt:Option[Session], callee:ActorRef): Unit = {
    val response = sessionOpt match {
      case Some(session) =>
        unregister(msg.registrationId) match {
          case Some(_) =>
            Unregistered(msg.requestId)

          case _ =>
            Error(UNREGISTER, msg.requestId, Json.obj(), Reason.NoSuchRegistration, None, None)
        }

      case _ =>
        Error(UNREGISTER, msg.requestId, Json.obj(), Reason.NotAuthorized, None, None)
    }

    callee ! response
  }

  def onError(msg:Error, sessionOpt:Option[Session], callee:ActorRef): Unit = {
    callee ! Error(CALL, msg.id, msg.details, msg.error, msg.argumentsOpt, msg.argumentsKwOpt)
  }

  def register(session:Session, r:Registration): Unit = {
    router.registrationsById.put(r.id, r)
    router.registrationsByProcedure.put(r.procedureName, r)

    session.registrationIds.add(r.id)

  }
  def unregister(registrationId:Long):Option[Registration] = {
    router.registrationsById.remove(registrationId).map { r =>
      router.registrationsByProcedure.remove(r.procedureName)
      r
    }
  }

}