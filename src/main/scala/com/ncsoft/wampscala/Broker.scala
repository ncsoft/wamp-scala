package com.ncsoft.wampscala

import akka.actor.ActorRef
import spray.json.{JsArray, JsObject}

import scala.collection.mutable

case class Subscription(id:Long)


class Broker(pubSubService: PubSubService) extends Role {
  val createdTopics = new mutable.HashSet[String]

  override def messageHandler(client:ActorRef) = {
    case msg:Publish =>
      // TODO: implement

    case msg:Subscribe =>
    // TODO: implement

    case msg:Unsubscribe =>
    // TODO: implement
  }
}