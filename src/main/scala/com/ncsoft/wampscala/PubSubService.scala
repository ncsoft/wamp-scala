package com.ncsoft.wampscala

import spray.json.{JsArray, JsObject}

import scala.collection.mutable

trait PubSubService {
  val subscribers = new mutable.HashMap[Long, Subscription]

  def subscribe(subscription:Subscription)
  def unsubscribe(id:Long)

  def publish(topic:String, publicationId:Long, options:JsObject, argumentsOpt:Option[JsArray],
              argumentsKwOpt:Option[JsObject]):Long
}
