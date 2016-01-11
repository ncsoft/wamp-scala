package com.ncsoft.wampscala

import spray.json._

import MessageCode._
import scala.util.Try


sealed abstract class Message {
  def code:MessageCode.Value

  def toJson:JsValue

  def serialize:String = toJson.toString()
}

object Message {

  def deserialize(payload:String):Message = {
    try {
      val j = JsonParser(payload).asInstanceOf[JsArray]
      val code = MessageCode(j.elements.head.asInstanceOf[Int])

      code match {
        case HELLO =>
          Hello(j.elements(1).asInstanceOf[String], j.elements(2).asInstanceOf[JsObject])

        case CHALLENGE =>
          Challenge(j.elements(1).asInstanceOf[String], j.elements(2).asInstanceOf[JsObject])

        case AUTHENTICATE =>
          Authenticate(j.elements(1).asInstanceOf[String], j.elements(2).asInstanceOf[JsObject])

        case WELCOME =>
          Welcome(j.elements(1).asInstanceOf[Long], j.elements(2).asInstanceOf[JsObject])

        case GOODBYE =>
          GoodBye(j.elements(1).asInstanceOf[JsObject], j.elements(2).asInstanceOf[String])

        case PUBLISH =>
          Publish(j.elements(1).asInstanceOf[Long], j.elements(2).asInstanceOf[JsObject],
            j.elements(3).asInstanceOf[String], Try(j.elements(4).asInstanceOf[JsArray]).toOption,
            Try(j.elements(5).asInstanceOf[JsObject]).toOption)

        case PUBLISHED =>
          Published(j.elements(1).asInstanceOf[Long], j.elements(2).asInstanceOf[Long])

        case SUBSCRIBE =>
          Subscribe(j.elements(1).asInstanceOf[Long], j.elements(2).asInstanceOf[JsObject], j.elements(3).asInstanceOf[String])

        case SUBSCRIBED =>
          Subscribed(j.elements(1).asInstanceOf[Long], j.elements(2).asInstanceOf[Long])

        case UNSUBSCRIBE =>
          Unsubscribe(j.elements(1).asInstanceOf[Long], j.elements(2).asInstanceOf[Long])

        case UNSUBSCRIBED =>
          Unsubscribed(j.elements(1).asInstanceOf[Long])

        case REGISTER =>
          Register(j.elements(1).asInstanceOf[Long], j.elements(2).asInstanceOf[JsObject], j.elements(3).asInstanceOf[String])

        case REGISTERED =>
          Registered(j.elements(1).asInstanceOf[Long], j.elements(2).asInstanceOf[Long])

        case UNREGISTER =>
          Unregister(j.elements(1).asInstanceOf[Long], j.elements(2).asInstanceOf[Long])

        case UNREGISTERED =>
          Unregistered(j.elements(1).asInstanceOf[Long])

        case YIELD =>
          Yield(j.elements(1).asInstanceOf[Long], j.elements(2).asInstanceOf[JsObject],
            Try(j.elements(3).asInstanceOf[JsArray]).toOption,
            Try(j.elements(4).asInstanceOf[JsObject]).toOption)

        case ERROR =>
          Error(j.elements(1).asInstanceOf[MessageCode], j.elements(2).asInstanceOf[Long],
            j.elements(3).asInstanceOf[JsObject], j.elements(4).asInstanceOf[String],
            Try(j.elements(5).asInstanceOf[JsArray]).toOption,
            Try(j.elements(6).asInstanceOf[JsObject]).toOption)

        case RESULT =>
          Result(j.elements(1).asInstanceOf[Long], j.elements(2).asInstanceOf[JsObject],
            Try(j.elements(3).asInstanceOf[JsArray]).toOption,
            Try(j.elements(4).asInstanceOf[JsObject]).toOption)

        case EVENT =>
          Event(j.elements(1).asInstanceOf[Long], j.elements(2).asInstanceOf[Long],
            j.elements(3).asInstanceOf[JsObject], Try(j.elements(4).asInstanceOf[JsArray]).toOption,
            Try(j.elements(5).asInstanceOf[JsObject]).toOption)

        case HEARTBEAT =>
          Heartbeat(j.elements(1).asInstanceOf[Long], j.elements(2).asInstanceOf[Long],
            Try(j.elements(3).asInstanceOf[String]).toOption)

        case CALL =>
          Call(j.elements(1).asInstanceOf[Long], j.elements(2).asInstanceOf[JsObject],
            j.elements(3).asInstanceOf[String], Try(j.elements(4).asInstanceOf[JsArray]).toOption,
            Try(j.elements(5).asInstanceOf[JsObject]).toOption)
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
        Some(JsString(topic)),
        argumentsOpt,
        argumentsKwOpt
      ).flatten
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
        Some(details),
        publishArgumentsOpt,
        publishArgumentsKwOpt
      ).flatten
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
        Some(JsString(procedure)),
        argumentsOpt,
        argumentsKwOpt
      ).flatten
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
        Some(details),
        argumentsOpt,
        argumentsKwOpt
      ).flatten
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
        Some(options),
        argumentsOpt,
        argumentsKwOpt
      ).flatten
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
        Some(details),
        argumentsOpt,
        argumentsKwOpt
      ).flatten
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
        Some(JsString(error)),
        argumentsOpt,
        argumentsKwOpt
      ).flatten
    )

}