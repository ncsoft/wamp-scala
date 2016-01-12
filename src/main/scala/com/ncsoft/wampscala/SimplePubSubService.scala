package com.ncsoft.wampscala

import play.api.libs.json.{JsArray, JsObject}

import scala.concurrent.Future
import scala.collection.mutable


class SimplePubSubService(eventHandler:Event => Any) extends PubSubService(eventHandler) {
  val topics = new mutable.HashMap[String, Topic] // topic, subscriptionId

  def subscribe(topic:String):Long = {
    val subscriptionId = IdGenerator(IdScope.Router)

    val topic = topics.getOrElseUpdate(topic, {
      new Topic(topic)
    })

    topic.addSubscriber(subscriptionId)

    subscriptionId
  }

  def unsubscribe(topic:String, id:Long) = {
    topics.get(topic).foreach { topic =>
      topic.removeSubscriber(id)
    }
  }

  def publish(topic:String, publicationId:Long, options:JsObject, argumentsOpt:Option[JsArray],
              argumentsKwOpt:Option[JsObject]):Long = {
    val publicationId = IdGenerator(IdScope.Global)

    Future {
      topics.get(topic).foreach { topic =>
        topic.subscribers.keys.foreach { subscriptionId =>
          val event = Event(subscriptionId, publicationId, )
        }
      }
    }

    publicationId
  }


}