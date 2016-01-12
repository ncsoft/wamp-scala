organization := "com.ncsoft.wampscala"

name := "wamp-scala"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.1",
//  "io.spray" %% "spray-json" % "1.3.2",
  "com.typesafe.play" % "play-json_2.11" % "2.4.6",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.1" % "test"
)

    