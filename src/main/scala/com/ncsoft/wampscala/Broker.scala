package com.ncsoft.wampscala

import akka.actor.ActorRef
import com.ncsoft.wampscala.MessageCode._
import play.api.libs.json._

import scala.collection.mutable

//case class Subscription(id:Long)

object TrustLevel {
  val Lowest = 0
  val Low = 1
  val Mid = 2
  val High = 3
  val Default = Lowest
}

object Broker {
  val createdTopics = new mutable.HashSet[String]
}


class Broker(router:Router, pubSubService: PubSubService) extends Role {
  object RealmUniqueTopic {
    def apply(realm:String, topic:String):String = {
      s"$realm..$topic"
    }
  }

  override def messageHandler(client:ActorRef) = {
    case msg:Publish =>     onPublish(msg, client)
    case msg:Subscribe =>   onSubscribe(msg, client)
    case msg:Unsubscribe => onUnsubscribe(msg, client)
  }

  protected def translatePublishOptions(options:JsObject, session:Session):JsObject = {

    val translated = options.value.toSeq.map { case (k, v) =>
      (k, v) match {
        case (Keyword.excludeMe, JsBoolean(true)) =>
          (Keyword.exclude, Json.arr(session.id))
        case (Keyword.discloseMe, JsBoolean(true)) =>
          (Keyword.publisher, JsNumber(session.id))
        case _ =>
          (k, v)
      }
    }.groupBy { x =>
      x._1
    }.map { case (k, v) =>
      k match {
        case Keyword.exclude =>
          (k, v.map(_._2.asOpt[JsArray].getOrElse(Json.arr())).reduce(_ ++ _))
        case _ =>
          v.head
      }
    }

    JsObject(translated.toSeq)
  }

  def onPublish(msg:Publish, client:ActorRef): Unit = {
    val response = router.sessionOpt match {
      case Some(session) =>
        try {
          if (!Uri.isValid(msg.topic))
            throw new InvalidUriException(msg.topic)

          val publicationId = pubSubService.publish(
            RealmUniqueTopic(session.realm, msg.topic),
            msg.requestId,
            translatePublishOptions(msg.options ++ Json.obj(Keyword.trustLevel -> TrustLevel.Default), session),
            msg.argumentsOpt,
            msg.argumentsKwOpt
          )

          Published(msg.requestId, publicationId)
        } catch {
          case t:WampException =>
            Error(PUBLISH, msg.requestId, Json.obj("message" -> t.toString), t.reason, None, None)

          case t:Throwable =>
            Error(PUBLISH, msg.requestId, Json.obj("message" -> t.getMessage), Reason.Unknown, None, None)
        }

      case _ =>
        Error(PUBLISH, msg.requestId, Json.obj(), Reason.NotAuthorized, None, None)

    }

    client ! response
  }

  def onSubscribe(msg:Subscribe, client:ActorRef): Unit = {
    val response = router.sessionOpt match {
      case Some(session) =>
        try {
          if (!Uri.isValid(msg.topic))
            throw new InvalidUriException(msg.topic)

          val subscriptionId = pubSubService.subscribe(RealmUniqueTopic(session.realm, msg.topic), client)
          session.subscriptionIds.add(subscriptionId)

          Subscribed(msg.requestId, subscriptionId)
        }  catch {
          case t:WampException =>
            Error(SUBSCRIBE, msg.requestId, Json.obj("message" -> t.toString), t.reason, None, None)

          case t:Throwable =>
            Error(SUBSCRIBE, msg.requestId, Json.obj("message" -> t.getMessage), Reason.Unknown, None, None)
        }
      case _ =>
        Error(SUBSCRIBE, msg.requestId, Json.obj(), Reason.NotAuthorized, None, None)
    }

    client ! response
  }

  def onUnsubscribe(msg:Unsubscribe, client:ActorRef): Unit = {
    val response = router.sessionOpt match {
      case Some(session) =>
        unsubscribe(session, msg.subscriptionId) match {
          case true =>
            Unsubscribed(msg.requestId)

          case false =>
            Error(UNSUBSCRIBE, msg.subscriptionId, Json.obj(), Reason.NoSuchSubscription, None, None)
        }

      case _ =>
        Error(UNSUBSCRIBE, msg.requestId, Json.obj(), Reason.NotAuthorized, None, None)
    }

    client ! response
  }

  def unsubscribe(session:Session, subscriptionId:Long): Boolean = {
    session.subscriptionIds.contains(subscriptionId) match {
      case true =>
        pubSubService.unsubscribe(subscriptionId)
        session.subscriptionIds.remove(subscriptionId)

        true

      case false =>
        false
    }
  }
}