name := "SbtStarter"

version := "1.0"

scalaVersion := "2.11.4"

scalacOptions ++= Seq("-deprecation", "-feature")

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.7"

Revolver.settings

libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "1.4.0"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.7"

libraryDependencies += "com.typesafe.akka" %% "akka-slf4j" % "2.3.7"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2"

libraryDependencies += "commons-io" % "commons-io" % "2.3"

