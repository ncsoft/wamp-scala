package com.ncsoft.wampscala

object Uri {
  def unapply(uri:String): Option[String] = {
    //    val uriPattern = "^([0-9a-z_]+[.])*([0-9a-z_]+)$".r   //strict URI
    val uriPattern = "^([^\\s\\.#]+\\.)*([^\\s\\.#]+)$".r //loose URI
    uriPattern.findFirstIn(uri)
  }

  def isValid(uri:String) = unapply(uri).isDefined
}