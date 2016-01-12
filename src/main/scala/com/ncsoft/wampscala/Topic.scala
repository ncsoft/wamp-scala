package com.ncsoft.wampscala

import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConverters._

class Topic(topic:String) {
  val subscribers = new ConcurrentHashMap[Long, Long].asScala

  def addSubscriber(id:Long) = subscribers.put(id, id)
  def removeSubscriber(id:Long) = subscribers.remove(id)
}