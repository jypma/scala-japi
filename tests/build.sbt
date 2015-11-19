name := "tests"

EclipseKeys.projectFlavor := EclipseProjectFlavor.Java

libraryDependencies ++= Seq(
    "junit" % "junit" % "4.11" % "test",
    "com.novocode" % "junit-interface" % "0.11" % "test"
)