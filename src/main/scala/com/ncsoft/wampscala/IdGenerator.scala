package com.ncsoft.wampscala

object IdScope {
  val Global = 1
  val Router = 2
  val Session = 3
  val Default = Router
}

//object IdGenerator {
//
//  protected var idGenerator:IdGenerator = null
//
//  def initialize(generator:IdGenerator) = {
//    idGenerator = generator
//  }
//
//  def apply(scope:Int = IdScope.Default): Long = idGenerator.nextId(scope)
//}

trait IdGenerator {
  def apply(scope:Int) = nextId(scope)

  def nextId(scope:Int): Long = {
    scope match {
      case IdScope.Global =>
        nextGlobalScopeId()
      case IdScope.Router =>
        nextRouterScopeId()
      case IdScope.Session =>
        nextSessionScopeId()
    }
  }

  def nextGlobalScopeId(): Long

  def nextRouterScopeId(): Long

  def nextSessionScopeId(): Long
}

class SimpleIdGenerator extends IdGenerator {
  def nextGlobalScopeId(): Long = {
    Math.floor(9007199254740991L * Math.random()).toLong
  }

  private var currentRouterScopeId = System.currentTimeMillis()

  def nextRouterScopeId(): Long = synchronized {
    currentRouterScopeId = currentRouterScopeId + 1
    currentRouterScopeId
  }

  private var currentSessionScopeId = 0L

  def nextSessionScopeId(): Long = synchronized {
    currentSessionScopeId = currentSessionScopeId + 1
    currentSessionScopeId
  }
}
