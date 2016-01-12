package com.ncsoft.wampscala

object Reason {

  protected var prefix = "wamp.error."

  def setPrefix(str:String) =
    prefix = str;

  def Unknown                   = prefix + "unknown"
  def GoodByeAndOut             = prefix + "goodbye_and_out"
  def NotAuthorized             = prefix + "not_authorized"
  def ProcedureAlreadyExists    = prefix + "procedure_already_exists"
  def NoSuchProcedure           = prefix + "no_such_procedure"
  def InvalidArgument           = prefix + "invalid_argument"
  def InvalidUri                = prefix + "invalid_uri"
  def NoSuchRole                = prefix + "no_such_role"
  def NoSuchRealm               = prefix + "no_such_realm"
  def SystemShutdown            = prefix + "system_shutdown"
  def CloseRealm                = prefix + "close_realm"
  def AuthorizationFailed       = prefix + "authorization_failed"
  def NoSuchSubscription        = prefix + "no_such_subscription"
  def NoSuchRegistration        = prefix + "no_such_registration"
  def NoEligibleCallee          = prefix + "no_eligible_callee"
}
