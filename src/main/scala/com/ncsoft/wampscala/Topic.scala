package com.ncsoft.wampscala

import java.util.concurrent.ConcurrentHashMap
import akka.actor.ActorRef

import scala.collection.JavaConverters._

case class Subscription(id:Long, client:ActorRef)

class Topic(topic:String) {
  val subscriptions = new ConcurrentHashMap[Long, Subscription].asScala

  def subscribe(id:Long, client:ActorRef) = subscriptions.put(id, Subscription(id, client))

  def unsubscribe(id:Long) = subscriptions.remove(id)
}