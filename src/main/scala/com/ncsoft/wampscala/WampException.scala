package com.ncsoft.wampscala

import Reason._

class WampException(messageOpt:Option[String] = None) extends Throwable {
 def reason:String = Unknown
 def message = toString
 override def toString = messageOpt.getOrElse("")
}

class InvalidArgumentException(message:String) extends WampException {
 override def reason = InvalidArgument
 override def toString = message
}

class InvalidUriException(invalidUri:String) extends WampException {
  override def reason = InvalidUri
  override def toString = s"Invalid URI($invalidUri) was provided."
}

class NotAuthorizedException() extends WampException {
 override def reason = NotAuthorized
 override def toString = "Not Authorized."
}

class NoSuchRealmException() extends WampException {
  override def reason = NoSuchRealm
  override def toString = s"The realm does not exist."
}

class NoSuchProcedureException(procedure:String) extends WampException {
 override def reason = NoSuchProcedure
 override def toString = s"No such procedure: $procedure"
}





