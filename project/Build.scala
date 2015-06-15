import sbt._
import sbt.Keys._
import android.Keys._
import android.Plugin._
import xerial.sbt.Sonatype._
import xerial.sbt.Sonatype.SonatypeKeys._

object	Build
extends	android.AutoBuild
{
	lazy val main = Project( "parcelable", file( "." ), settings = androidBuildAar ++ sonatypeSettings )
		.settings(
			fork in Test := true,
			javacOptions ++= (
				"-source" :: "1.7" ::
				"-target" :: "1.7" ::
				Nil
			),
			libraryDependencies <++= scalaVersion( version =>
				compilerPlugin( "org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full ) ::
				"com.android.support" % "support-v4" % "22.2.0" % "test" ::
				"com.geteit" %% "robotest" % "0.7" % "test" ::
				"junit" % "junit" % "4.12" % "test" ::
				"org.scala-lang" % "scala-compiler" % version ::
				"org.scala-lang" % "scala-reflect" % version ::
				"org.robolectric" % "android-all" % "5.0.0_r2-robolectric-0" % "provided" ::
				"org.scalatest" %% "scalatest" % "2.2.5" % "test" ::
				Nil
			),
			name := "Parcelable",
			organization := "io.taig.android",
			resolvers += "RoboTest" at "https://raw.github.com/zbsz/mvn-repo/master/releases/",
			scalaVersion := "2.11.6",
			scalacOptions ++= (
				"-deprecation" ::
				"-feature" ::
				Nil
			),
			version := "2.2.0"
		)
		.settings(
			minSdkVersion in Android := "4",
			platformTarget in Android := "android-22",
			sourceGenerators in Compile <<= ( sourceGenerators in Compile ) ( generators => Seq( generators.last ) ),
			targetSdkVersion in Android := "22",
			typedResources in Android := false
		)
		.settings(
			description := "Parcelable compile time code generation for Scala on Android",
			homepage := Some( url( "https://github.com/taig/parcelable" ) ),
			licenses := Seq( "MIT" -> url( "https://raw.githubusercontent.com/taig/parcelable/master/LICENSE" ) ),
			organizationHomepage := Some( url( "http://taig.io" ) ),
			pomExtra :=
			{
				<issueManagement>
					<url>https://github.com/taig/parcelable/issues</url>
					<system>GitHub Issues</system>
				</issueManagement>
				<developers>
					<developer>
						<id>Taig</id>
						<name>Niklas Klein</name>
						<email>mail@taig.io</email>
						<url>http://taig.io/</url>
					</developer>
				</developers>
			},
			pomIncludeRepository := { _ => false },
			sonatypeProfileName := "io.taig",
			publishArtifact in Test := false,
			publishMavenStyle := true,
			publishTo <<= version ( version =>
			{
				val url = Some( "https://oss.sonatype.org/" )

				if( version.endsWith( "SNAPSHOT" ) )
				{
					url.map( "snapshot" at _ + "content/repositories/snapshots" )
				}
				else
				{
					url.map( "release" at _ + "service/local/staging/deploy/maven2" )
				}
			} ),
			scmInfo := Some(
				ScmInfo(
					url( "https://github.com/taig/parcelable" ),
					"scm:git:git://github.com/taig/parcelable.git",
					Some( "scm:git:git@github.com:taig/parcelable.git" )
				)
			),
			startYear := Some( 2015 )
		)
}