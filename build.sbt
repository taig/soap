enablePlugins( AndroidLib )

fork in Test := true

githubProject := "soap"

javacOptions ++=
    "-source" :: "1.7" ::
    "-target" :: "1.7" ::
    Nil

libraryDependencies ++=
    List( "core", "generic", "parser" ).map { id =>
        "io.circe" %% s"circe-$id" % "0.7.0"
    } :::
    "com.geteit" %% "robotest" % "0.12" % "test" ::
    "org.scalatest" %% "scalatest" % "3.0.1" % "test" ::
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

scalaVersion := "2.11.11"

testOptions in Test += Tests.Argument( "-oDF" )

typedResources := false