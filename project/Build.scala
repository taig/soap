import android.Keys._
import android.Plugin._
import sbt.Keys._
import sbt._

object	Build
extends	android.AutoBuild
{
	lazy val main = Project( "parcelable", file( "." ), settings = androidBuildAar )
		.settings(
			fork in Test := true,
			libraryDependencies <++= scalaVersion( version =>
				Seq(
					compilerPlugin( "org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full ),
					"org.scala-lang" % "scala-compiler" % version,
					"org.scala-lang" % "scala-reflect" % version,
					"com.android.support" % "support-v4" % "21.0.0" % "test",
					"org.robolectric" % "android-all" % "5.0.0_r2-robolectric-0" % "provided",
					"junit" % "junit" % "4.8.2" % "test",
					"org.scalatest" %% "scalatest" % "2.1.6" % "test",
					"com.geteit" %% "robotest" % "0.7" % "test"
				)
			),
			name := "Parcelable",
			organization := "com.taig.android",
			publishArtifact in ( Compile, packageDoc ) := false,
			resolvers ++= Seq(
				Resolver.sonatypeRepo( "releases" ),
				"Typesafe" at "http://repo.typesafe.com/typesafe/releases/",
				"RoboTest" at "https://raw.github.com/zbsz/mvn-repo/master/releases/"
			),
			scalaVersion := "2.11.5",
			scalacOptions ++= Seq(
				"-deprecation",
				"-feature",
				"-language:existentials",
				"-language:implicitConversions",
				"-language:experimental.macros",
				"-language:reflectiveCalls"
			),
			sourceGenerators in Compile <<= ( sourceGenerators in Compile ) ( generators => Seq( generators.last ) ),
			version := "1.1.0",
			minSdkVersion in Android := "4",
			platformTarget in Android := "android-21",
			typedResources in Android := false
		)
}