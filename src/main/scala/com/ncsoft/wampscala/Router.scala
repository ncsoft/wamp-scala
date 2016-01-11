package com.ncsoft.wampscala

import akka.actor.{Actor, ActorRef}
import spray.json.JsObject

class Router(pubSubService: PubSubService) extends Actor {
  val broker = new Broker(pubSubService)
  val dealer = new Dealer


  protected def messageHandler(client:ActorRef):PartialFunction[Message, Unit] = {
    // TODO: implement
    case msg: Hello =>
      client ! Welcome(0L, JsObject())

    // TODO: implement
    case msg: Authenticate =>

    // TODO: implement
    case msg: GoodBye =>
  }

  protected def integratedMessageHandler(client:ActorRef) =
    messageHandler(client) orElse
      broker.messageHandler(client) orElse
      dealer.messageHandler(client)

  def receive:Receive = {
    case wampMsg:Message =>
      val client = sender()
      integratedMessageHandler(client)(wampMsg)

  }



}