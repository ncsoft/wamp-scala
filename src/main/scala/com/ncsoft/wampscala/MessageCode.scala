package com.ncsoft.wampscala

object MessageCode extends Enumeration {
  type MessageCode = Value

  val HELLO         = Value(1)
  val WELCOME       = Value(2)
  val ABORT         = Value(3)
  val CHALLENGE     = Value(4)
  val AUTHENTICATE  = Value(5)
  val GOODBYE       = Value(6)
  val HEARTBEAT     = Value(7)
  val ERROR         = Value(8)
  val PUBLISH       = Value(16)
  val PUBLISHED     = Value(17)
  val SUBSCRIBE     = Value(32)
  val SUBSCRIBED    = Value(33)
  val UNSUBSCRIBE   = Value(34)
  val UNSUBSCRIBED  = Value(35)
  val EVENT         = Value(36)
  val CALL          = Value(48)
  val CANCEL        = Value(49)
  val RESULT        = Value(50)
  val REGISTER      = Value(64)
  val REGISTERED    = Value(65)
  val UNREGISTER    = Value(66)
  val UNREGISTERED  = Value(67)
  val INVOCATION    = Value(68)
  val INTERRUPT     = Value(69)
  val YIELD         = Value(70)
  val INVALID       = Value(1023)

//  implicit val reads:Reads[MessageCode.Value] = JsPath.read[Int].map { x => MessageCode(x) }
}