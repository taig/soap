fork in Test := true

javacOptions ++=
    "-source" :: "1.7" ::
    "-target" :: "1.7" ::
    Nil

libraryDependencies ++=
    compilerPlugin( "org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full ) ::
    "com.chuusai" %% "shapeless" % "2.2.5" ::
    "org.julienrf" %% "enum" % "1.1" ::
    "org.scala-lang" % "scala-reflect" % scalaVersion.value ::
    "org.typelevel" %% "export-hook" % "1.1.0" ::
    "com.geteit" %% "robotest" % "0.12" % "test" ::
    "org.scalatest" %% "scalatest" % "2.2.5" % "test" ::
    Nil

name := "Parcelable"

organization := "io.taig.android"

scalaVersion := "2.11.7"

scalacOptions ++=
    "-deprecation" ::
    "-feature" ::
    Nil

testOptions in Test += Tests.Argument( "-oDF" )

version := "3.0.0-SNAPSHOT"