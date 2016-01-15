package com.ncsoft.wampscala

import java.util.Date

import akka.actor.ActorRef
import scala.collection.mutable

trait BaseSession {
  def id:Long
  def realm:String

  var peerActor:ActorRef = null
  var outActor:ActorRef = null
}

case class Session(id:Long, realm:String, router:ActorRef, client:ActorRef,
                   subscriptionIds:mutable.Set[Long], registrationIds:mutable.Set[Long])

case class SimpleSession(id:Long, realm:String, createdAt:Date) extends BaseSession
