package com.ncsoft.wampscala

import akka.actor.ActorRef
import play.api.libs.json.{JsArray, JsObject}

import scala.concurrent.ExecutionContext

abstract class PubSubService(ec:ExecutionContext) {
  def subscribe(realm:String, topic:String, client:ActorRef):Long
  def unsubscribe(id:Long)

  def publish(realm:String, topic:String, publicationId:Long, options:JsObject, argumentsOpt:Option[JsArray],
              argumentsKwOpt:Option[JsObject]):Long
}
