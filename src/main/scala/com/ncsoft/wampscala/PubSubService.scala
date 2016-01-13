package com.ncsoft.wampscala

import akka.actor.ActorRef
import play.api.libs.json.{JsArray, JsObject}

import scala.concurrent.ExecutionContext

abstract class PubSubService(eventHandler: (Long, Event) => Any, ec:ExecutionContext) {
  def subscribe(topic:String, client:ActorRef):Long
  def unsubscribe(id:Long)

  def publish(topic:String, publicationId:Long, options:JsObject, argumentsOpt:Option[JsArray],
              argumentsKwOpt:Option[JsObject]):Long
}
