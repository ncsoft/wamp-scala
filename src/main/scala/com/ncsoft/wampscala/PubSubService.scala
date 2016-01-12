package com.ncsoft.wampscala

import play.api.libs.json.{JsArray, JsObject}

import scala.collection.mutable

abstract class PubSubService(eventHandler: Event => Any) {
  def subscribe(topic:String):Long
  def unsubscribe(topic:String, id:Long)

  def publish(topic:String, publicationId:Long, options:JsObject, argumentsOpt:Option[JsArray],
              argumentsKwOpt:Option[JsObject]):Long
}
