package com.ncsoft.wampscala

import akka.actor.ActorRef

trait Role {
  def messageHandler(peer:ActorRef):PartialFunction[Message, Unit]
}

