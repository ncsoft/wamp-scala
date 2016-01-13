package com.ncsoft.wampscala

import akka.actor.ActorRef

class Dealer(router:Router) extends Role {
  def messageHandler(client:ActorRef) = {
    case msg: Call =>       onCall(msg, client)
    case msg: Yield =>      onYield(msg, client)
    case msg: Register =>   onRegister(msg, client)
    case msg: Unregister => onUnregister(msg, client)
    case msg: Error =>      onError(msg, client)
  }

  def onCall(msg:Call, client:ActorRef): Unit = {

  }

  def onYield(msg:Yield, client:ActorRef): Unit = {

  }

  def onRegister(msg:Register, client:ActorRef): Unit = {

  }

  def onUnregister(msg:Unregister, client:ActorRef): Unit = {

  }

  def onError(msg:Error, client:ActorRef): Unit = {

  }
}