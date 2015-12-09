name := "tests"

EclipseKeys.projectFlavor := EclipseProjectFlavor.Java

EclipseKeys.executionEnvironment := Some(EclipseExecutionEnvironment.JavaSE18)

libraryDependencies ++= Seq(
    "junit" % "junit" % "4.11" % "test",
    "com.insightfullogic" % "lambda-behave" % "0.4" % "test",
    "org.assertj" % "assertj-core" % "3.2.0" % "test",
    "com.novocode" % "junit-interface" % "0.11" % "test"    
)

// Ensure compilation with java 8
javacOptions ++= Seq("-source", "1.8")
javacOptions in (Compile, Keys.compile) ++= Seq("-target", "1.8", "-Xlint")
