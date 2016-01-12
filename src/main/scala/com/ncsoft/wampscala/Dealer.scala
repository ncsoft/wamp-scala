package com.ncsoft.wampscala

import akka.actor.ActorRef

class Dealer(router:Router) extends Role {
  def messageHandler(client:ActorRef) = {
    case msg: Call =>
      // TODO: implement

    case msg: Yield =>
    // TODO: implement

    case msg: Register =>
    // TODO: implement

    case msg: Unregister =>
    // TODO: implement

    case msg: Error =>
    // TODO: implement
  }
}