name := "akkaStreams"

libraryDependencies ++= {
  val akkaVersion = "2.4.0"
  val akkaStreamVersion = "1.0"

  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaStreamVersion
  )
}
