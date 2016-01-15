//package com.ncsoft.wampscala
//
//import akka.actor.{Actor, ActorRef}
//
//import scala.collection.mutable
//
//
//object Callee {
//
//}
//
//class Callee(procedure:Procedure) {
//  var registrationIdOpt:Option[Long] = None
//
//  def messageHandler(dealer:ActorRef) = {
//    case msg:Registered =>    onRegistered(msg, dealer)
//    case msg:Unregistered =>  onUnregistered(msg, dealer)
//    case msg:Invocation =>    onInvocation(msg, dealer)
//  }
//
//  def onRegistered(msg:Registered, dealer:ActorRef): Unit = {
//    registrationIdOpt = Some(msg.registrationId)
//  }
//
//  def onUnregistered(msg:Unregistered, dealer:ActorRef): Unit = {
//    registrationIdOpt = None
//  }
//
//  def onInvocation(msg:Invocation, dealer:ActorRef): Unit = {
//    dealer ! procedure.action(msg)
//  }
//}