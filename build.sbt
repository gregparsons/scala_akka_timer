name := "scala_akka_timer"

version := "0.1"

scalaVersion := "2.13.5"


lazy val akkaVersion = "2.6.14"

libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2"
libraryDependencies ++= Seq(
	"com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
	"ch.qos.logback" % "logback-classic" % "1.2.3",
	"com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
	"org.scalatest" %% "scalatest" % "3.1.0" % Test
)

// for scheduler
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % akkaVersion