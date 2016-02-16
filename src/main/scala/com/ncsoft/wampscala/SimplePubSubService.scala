package com.ncsoft.wampscala

import akka.actor.ActorRef
import play.api.libs.json.{Json, JsArray, JsObject}

import scala.concurrent.{ExecutionContext, Future}
import scala.collection.mutable


class SimplePubSubService(idGenerator:IdGenerator, ec:ExecutionContext)
  extends PubSubService(ec) {

  val topics = new mutable.HashMap[String, Topic]
  val subscribedTopics = new mutable.HashMap[Long, Topic]

  def topicKey(realm:String, topic:String):String = s"$realm-$topic"

  def subscribe(realm:String, topicString:String, client:ActorRef):Long = {
    val subscriptionId = idGenerator(IdScope.Router)

    val k = topicKey(realm, topicString)
    val topic = topics.getOrElseUpdate(k, {
      new Topic(topicString)
    })

    topic.subscribe(subscriptionId, client)
    subscribedTopics.put(subscriptionId, topic)

    subscriptionId
  }

  def unsubscribe(id:Long) = {
    subscribedTopics.get(id).foreach { topic =>
      topic.unsubscribe(id)

      subscribedTopics.remove(id)
    }
  }

  def publish(realm:String, topic:String, requestId:Long, options:JsObject, argumentsOpt:Option[JsArray],
              argumentsKwOpt:Option[JsObject]):Long = {
    val publishedId = idGenerator.nextGlobalScopeId()

    Future {

      topics.get(topicKey(realm, topic)).foreach { topic =>
        val eligibleSessions = (options \ Keyword.eligible).asOpt[JsArray].getOrElse(Json.arr()).value.map(_.as[Long])
        val excludeSessions = (options \ Keyword.exclude).asOpt[JsArray].getOrElse(Json.arr()).value.map(_.as[Long])

        topic.subscriptions.values.filter { s =>
          eligibleSessions.contains(s.id) || eligibleSessions.isEmpty
        }.filterNot { s =>
          excludeSessions.contains(s.id)
        }.foreach { s =>
          val event = Event(
            s.id,
            publishedId,
            options - Keyword.eligible - Keyword.exclude,
            argumentsOpt,
            argumentsKwOpt
          )

          s.client ! event
        }
      }
    } (ec)

    publishedId
  }


}