enablePlugins( AndroidLib )

fork in Test := true

githubProject := "soap"

javacOptions ++=
    "-source" :: "1.7" ::
    "-target" :: "1.7" ::
    Nil

libraryDependencies ++=
    "io.circe" %% "circe-core" % "0.5.4" ::
    "io.circe" %% "circe-generic" % "0.5.4" ::
    "io.circe" %% "circe-parser" % "0.5.4" ::
    "com.geteit" %% "robotest" % "0.12" % "test" ::
    "org.scalatest" %% "scalatest" % "3.0.0" % "test" ::
    Nil

minSdkVersion := "4"

name := "soap"

organization := "io.taig.android"

platformTarget := "android-24"

scalacOptions ++=
    "-deprecation" ::
    "-feature" ::
    "-optimize" ::
    Nil

scalaVersion := "2.11.8"

testOptions in Test += Tests.Argument( "-oDF" )

typedResources := false