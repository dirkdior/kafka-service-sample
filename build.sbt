name := "kafka-service-sample"

version := "0.1"

scalaVersion := "2.13.8"

val akkaVersion     = "2.6.18"
val akkaHttpVersion = "10.2.6"
val circeVersion    = "0.14.1"
val alpakkaVersion  = "3.0.0"

resolvers += Resolver.bintrayRepo("cakesolutions", "maven")

libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed"  % akkaVersion
libraryDependencies += "com.typesafe.akka" %% "akka-stream-kafka" % alpakkaVersion
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)
