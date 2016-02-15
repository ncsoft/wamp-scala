package com.ncsoft.wampscala

import play.api.libs.json._

import MessageCode._
import scala.util.Try


sealed abstract class Message {
  def code:MessageCode.Value

  def toJson:JsValue

  def serialize:String = toJson.toString()

  // argumentsOpt must be defined if arguemntsKwOpt is defined
  def serializeArguments(argumentsOpt:Option[JsArray], argumentsKwOpt:Option[JsObject]) = {
    if (argumentsOpt.isEmpty && argumentsKwOpt.isDefined)
      Vector(Some(JsArray()), argumentsKwOpt).flatten
    else
      Vector(argumentsOpt, argumentsKwOpt).flatten
  }

}

object Message {

  def deserialize(payload:String):Message = {
    try {
      val j = Json.parse(payload).as[JsArray]
      val code = MessageCode(j.value.head.as[Int])

      code match {
        case HELLO =>
          Hello(j(1).as[String], j(2).as[JsObject])

        case CHALLENGE =>
          Challenge(j(1).as[String], j(2).as[JsObject])

        case AUTHENTICATE =>
          Authenticate(j(1).as[String], j(2).as[JsObject])

        case WELCOME =>
          Welcome(j(1).as[Long], j(2).as[JsObject])

        case GOODBYE =>
          GoodBye(j(1).as[JsObject], j(2).as[String])

        case PUBLISH =>
          Publish(j(1).as[Long], j(2).as[JsObject],
            j(3).as[String], j(4).asOpt[JsArray],
            j(5).asOpt[JsObject])

        case PUBLISHED =>
          Published(j(1).as[Long], j(2).as[Long])

        case SUBSCRIBE =>
          Subscribe(j(1).as[Long], j(2).as[JsObject], j(3).as[String])

        case SUBSCRIBED =>
          Subscribed(j(1).as[Long], j(2).as[Long])

        case UNSUBSCRIBE =>
          Unsubscribe(j(1).as[Long], j(2).as[Long])

        case UNSUBSCRIBED =>
          Unsubscribed(j(1).as[Long])

        case REGISTER =>
          Register(j(1).as[Long], j(2).as[JsObject], j(3).as[String])

        case REGISTERED =>
          Registered(j(1).as[Long], j(2).as[Long])

        case UNREGISTER =>
          Unregister(j(1).as[Long], j(2).as[Long])

        case UNREGISTERED =>
          Unregistered(j(1).as[Long])

        case YIELD =>
          Yield(j(1).as[Long], j(2).as[JsObject],
            j(3).asOpt[JsArray],
            j(4).asOpt[JsObject])

        case ERROR =>
          Error(j(1).as[MessageCode], j(2).as[Long],
            j(3).as[JsObject], j(4).as[String],
            j(5).asOpt[JsArray],
            j(6).asOpt[JsObject])

        case RESULT =>
          Result(j(1).as[Long], j(2).as[JsObject],
            j(3).asOpt[JsArray],
            j(4).asOpt[JsObject])

        case EVENT =>
          Event(j(1).as[Long], j(2).as[Long],
            j(3).as[JsObject], j(4).asOpt[JsArray],
            j(5).asOpt[JsObject])

        case HEARTBEAT =>
          Heartbeat(j(1).as[Long], j(2).as[Long],
            j(3).asOpt[String])

        case CALL =>
          Call(j(1).as[Long], j(2).as[JsObject],
            j(3).as[String], j(4).asOpt[JsArray],
            j(5).asOpt[JsObject])
      }
    } catch {
      case e:Throwable =>
//        Logger.debug(s"deserialize error: ${e.getMessage}")
        InvalidMessage(payload)
    }
  }

  def serialize(m:Message):String = m.serialize
}

case class InvalidMessage(payload:String) extends Message {
  def code = INVALID

  def toJson = JsString("")
}

case class Hello(realm:String, details:JsObject) extends Message {
  def code = HELLO

  def toJson =
    JsArray(
      Vector(
        JsNumber(code.id),
        JsString(realm),
        details
      )
    )

}

case class Welcome(sessionId:Long, details:JsObject) extends Message {
  def code = WELCOME

  def toJson =
    JsArray(
      Vector(
        JsNumber(code.id),
        JsNumber(sessionId), details)
    )
}

case class Abort(details:JsObject, reason:String) extends Message {
  def code = ABORT

  def toJson =
    JsArray(
      Vector(
        JsNumber(code.id),
        details,
        JsString(reason)
      )
    )
}

case class Challenge(challenge:String, extra:JsObject) extends Message {
  def code = CHALLENGE

  def toJson =
    JsArray(
      Vector(
        JsNumber(code.id),
        JsString(challenge),
        extra
      )
    )
}

case class Authenticate(signature:String, extra:JsObject) extends Message {
  def code = AUTHENTICATE

  def toJson =
    JsArray(
      Vector(
        JsNumber(code.id),
        JsString(signature),
        extra
      )
    )
}

case class Heartbeat(incomingSeq:Long, outgoingSeq:Long, discardOpt:Option[String]) extends Message {
  def code = HEARTBEAT

  def toJson =
    JsArray(
      Vector(
        Some(JsNumber(code.id)),
        Some(JsNumber(incomingSeq)),
        Some(JsNumber(outgoingSeq)),
        discardOpt.map { s => JsString(s) }
      ).flatten
    )
}

case class GoodBye(details:JsObject, reason:String) extends Message {
  def code = GOODBYE

  def toJson =
    JsArray(
      Vector(
        JsNumber(code.id),
        details,
        JsString(reason)
      )
    )
}

case class Subscribe(requestId:Long, options:JsObject, topic:String) extends Message {
  def code = SUBSCRIBE

  def toJson =
    JsArray(
      Vector(
        JsNumber(code.id),
        options,
        JsString(topic)
      )
    )
}

case class Subscribed(requestId:Long, subscriptionId:Long) extends Message {
  def code = SUBSCRIBED

  def toJson =
    JsArray(
      Vector(
        JsNumber(code.id),
        JsNumber(requestId),
        JsNumber(subscriptionId)
      )
    )
}

case class Unsubscribe(requestId:Long, subscriptionId:Long) extends Message {
  def code = UNSUBSCRIBE

  def toJson =
    JsArray(
      Vector(
        JsNumber(code.id),
        JsNumber(subscriptionId)
      )
    )
}

case class Unsubscribed(requestId:Long) extends Message {
  def code = UNSUBSCRIBED

  def toJson =
    JsArray(
      Vector(
        JsNumber(code.id),
        JsNumber(requestId)
      )
    )
}

case class Publish(requestId:Long, options:JsObject, topic:String, argumentsOpt:Option[JsArray],
                   argumentsKwOpt:Option[JsObject]) extends Message {
  def code = PUBLISH

  def toJson =
    JsArray(
      Vector(
        Some(JsNumber(code.id)),
        Some(options),
        Some(JsString(topic))
      ).flatten ++ serializeArguments(argumentsOpt, argumentsKwOpt)
    )
}

case class Published(requestId:Long, publicationId:Long) extends Message {
  def code = PUBLISHED

  def toJson =
    JsArray(
      Vector(
        JsNumber(code.id),
        JsNumber(requestId),
        JsNumber(publicationId)
      )
    )
}

case class Event(subscriptionId:Long, publicationId:Long, details:JsObject,
                 publishArgumentsOpt:Option[JsArray], publishArgumentsKwOpt:Option[JsObject]) extends Message {
  def code = EVENT

  def toJson =
    JsArray(
      Vector(
        Some(JsNumber(code.id)),
        Some(JsNumber(subscriptionId)),
        Some(JsNumber(publicationId)),
        Some(details)
      ).flatten ++ serializeArguments(publishArgumentsOpt, publishArgumentsKwOpt)
    )
}

case class Call(requestId:Long, options:JsObject, procedure:String,
                argumentsOpt:Option[JsArray], argumentsKwOpt:Option[JsObject]) extends Message {
  def code = CALL

  def toJson =
    JsArray(
      Vector(
        Some(JsNumber(code.id)),
        Some(JsNumber(requestId)),
        Some(options),
        Some(JsString(procedure))
      ).flatten ++ serializeArguments(argumentsOpt, argumentsKwOpt)
    )
}

case class Register(requestId:Long, options:JsObject, procedure:String) extends Message {
  def code = REGISTER

  def toJson =
    JsArray(
      Vector(
        JsNumber(code.id),
        JsNumber(requestId),
        options,
        JsString(procedure)
      )
    )
}

case class Registered(requestId:Long, registrationId:Long) extends Message {
  def code = REGISTERED

  def toJson =
    JsArray(
      Vector(
        JsNumber(code.id),
        JsNumber(requestId),
        JsNumber(registrationId)
      )
    )
}

case class Unregister(requestId:Long, registrationId:Long) extends Message {
  def code = UNREGISTER

  def toJson =
    JsArray(
      Vector(
        JsNumber(code.id),
        JsNumber(requestId),
        JsNumber(registrationId)
      )
    )
}

case class Unregistered(requestId:Long) extends Message {
  def code = UNREGISTERED

  def toJson =
    JsArray(
      Vector(
        JsNumber(code.id),
        JsNumber(requestId)
      )
    )
}

case class Invocation(requestId:Long, registrationId:Long, details:JsObject, argumentsOpt:Option[JsArray],
                      argumentsKwOpt:Option[JsObject]) extends Message {
  def code = INVOCATION

  def toJson =
    JsArray(
      Vector(
        Some(JsNumber(code.id)),
        Some(JsNumber(requestId)),
        Some(JsNumber(registrationId)),
        Some(details)
      ).flatten ++ serializeArguments(argumentsOpt, argumentsKwOpt)
    )
}

case class Yield(requestId:Long, options:JsObject, argumentsOpt:Option[JsArray],
                 argumentsKwOpt:Option[JsObject]) extends Message {
  def code = YIELD

  def toJson =
    JsArray(
      Vector(
        Some(JsNumber(code.id)),
        Some(JsNumber(requestId)),
        Some(options)
      ).flatten ++ serializeArguments(argumentsOpt, argumentsKwOpt)
    )
}

case class Result(requestId:Long, details:JsObject, argumentsOpt:Option[JsArray],
                  argumentsKwOpt:Option[JsObject]) extends Message {
  def code = RESULT

  def toJson =
    JsArray(
      Vector(
        Some(JsNumber(code.id)),
        Some(JsNumber(requestId)),
        Some(details)
      ).flatten ++ serializeArguments(argumentsOpt, argumentsKwOpt)
    )
}

case class Error(subCode:MessageCode, id:Long, details:JsObject, error:String, argumentsOpt:Option[JsArray],
                 argumentsKwOpt:Option[JsObject]) extends Message {
  def code = ERROR

  def toJson =
    JsArray(
      Vector(
        Some(JsNumber(code.id)),
        Some(JsNumber(subCode.id)),
        Some(JsNumber(id)),
        Some(details),
        Some(JsString(error))
      ).flatten ++ serializeArguments(argumentsOpt, argumentsKwOpt)
    )

}