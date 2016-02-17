name := "akkaStreamsTest"

libraryDependencies ++= {
  val akkaVersion = "2.4.2"
  val akkaStreamVersion = "2.4.2"

  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaStreamVersion
  )
}
