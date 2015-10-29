import sbt._
import Keys._
import com.typesafe.sbteclipse.plugin.EclipsePlugin._

object ScalaJApi extends Build {
  lazy val commonSettings = Seq(
    organization := "com.tradeshift.scala-japi",
    version := "0.1-SNAPSHOT",
    scalaVersion := "2.11.7",
    EclipseKeys.executionEnvironment := Some(EclipseExecutionEnvironment.JavaSE18),
    EclipseKeys.withSource := true
  )

  lazy val akkaStreams = project.settings(commonSettings: _*)
  
  lazy val scalaLib = project.settings(commonSettings: _*)
  
  lazy val scalaLibTest = project.settings(commonSettings: _*).dependsOn(scalaLib)
  
  lazy val tests = project.settings(commonSettings: _*).dependsOn(scalaLib, akkaStreams)
}