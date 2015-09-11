import sbt._
import sbt.Keys._
import android.Keys._
import android.Plugin._
import xerial.sbt.Sonatype._
import xerial.sbt.Sonatype.SonatypeKeys._

object	Build
extends	sbt.Build
{
	lazy val main = Project( "parcelable", file( "." ) )
		.settings(
			fork in Test := true,
			javacOptions ++= (
				"-source" :: "1.7" ::
				"-target" :: "1.7" ::
				Nil
			),
			libraryDependencies <++= scalaVersion( version =>
				compilerPlugin( "org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full ) ::
				"com.chuusai" %% "shapeless" % "2.2.5" ::
				"org.scala-lang" % "scala-reflect" % version ::
				"com.android.support" % "support-v4" % "23.0.1" % "test" ::
				"com.geteit" %% "robotest" % "0.12" % "test" ::
				"org.scalatest" %% "scalatest" % "2.2.5" % "test" ::
				Nil
			),
			name := "Parcelable",
			organization := "io.taig.android",
			resolvers ++= (
				Resolver.sonatypeRepo( "snapshots" ) ::
				( "RoboTest" at "https://raw.github.com/zbsz/mvn-repo/master/releases/" ) ::
				Nil
			),
			scalaVersion := "2.11.7",
			scalacOptions ++= (
				"-deprecation" ::
				"-feature" ::
				Nil
			),
			version := "2.4.0-SNAPSHOT"
		)
}