package com.ncsoft.wampscala

import spray.json.{JsArray, JsObject}

class AkkaPubSubService extends PubSubService {

  // TODO: implement
  def subscribe(subscription:Subscription) = {}

  // TODO: implement
  def unsubscribe(id:Long) = {}

  // TODO: implement
  def publish(topic:String, publicationId:Long, options:JsObject, argumentsOpt:Option[JsArray],
              argumentsKwOpt:Option[JsObject]):Long = {
    0L
  }
}