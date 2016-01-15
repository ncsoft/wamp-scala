package com.ncsoft.wampscala

import play.api.libs.json.Json

case class Procedure(name:String, action:Invocation => Message)

//object Procedure {
//  val systemProcedurePrefix = "wamp.session"
//
//  val list = Procedure(
//    systemProcedurePrefix + ".list",
//    { msg:Invocation =>
//      // TODO: implement
//      Right(Yield(msg.requestId, Json.obj(), None, None))
//    }
//  )
//
//  val count = Procedure(
//  systemProcedurePrefix + ".count",
//  { msg:Invocation =>
//    // TODO: implement
//    Right(Yield(msg.requestId, Json.obj(), None, None))
//  }
//  )
//
//  val kill = Procedure(
//  systemProcedurePrefix + ".kill",
//  { msg:Invocation =>
//    // TODO: implement
//    Right(Yield(msg.requestId, Json.obj(), None, None))
//  }
//  )
//
//  val systemProcedures = Map(
//    list.name -> list,
//    count.name -> count,
//    kill.name -> count
//  )
//}