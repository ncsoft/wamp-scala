package com.ncsoft.wampscala

import java.util.Date

import akka.actor.ActorRef

trait BaseSession {
  def id:Long
  def realm:String

  var peerActor:ActorRef = null
  var outActor:ActorRef = null
}

case class Session(id:Long, realm:String, router:ActorRef, client:ActorRef)

case class SimpleSession(id:Long, realm:String, createdAt:Date) extends BaseSession
