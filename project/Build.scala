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
			description := "Parcelable compile time code generation for Scala on Android",
			fork in Test := true,
			homepage := Some( url( "https://github.com/taig/parcelable" ) ),
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
			licenses := Seq( "MIT" -> url( "https://raw.githubusercontent.com/taig/parcelable/master/LICENSE" ) ),
			// see https://github.com/pfn/android-sdk-plugin/issues/151
			// name := "Parcelable",
			organization := "io.taig.android",
			organizationHomepage := Some( url( "http://taig.io" ) ),
			pomExtra := pom,
			pomIncludeRepository := { _ => false },
			profileName := "io.taig",
			publishArtifact in Test := false,
			publishMavenStyle := true,
			publishTo <<= version ( version =>
			{
				val repository = if( version.endsWith( "SNAPSHOT" ) )
				{
					( "snapshot", "/content/repositories/snapshots" )
				}
				else
				{
					( "release", "/service/local/staging/deploy/maven2" )
				}

				Some( repository._1 at "https://oss.sonatype.org" + repository._2 )
			} ),
			resolvers ++= Seq(
				Resolver.sonatypeRepo( "releases" ),
				"Typesafe" at "http://repo.typesafe.com/typesafe/releases/",
				"RoboTest" at "https://raw.github.com/zbsz/mvn-repo/master/releases/"
			),
			scalaVersion := "2.11.6",
			scalacOptions ++= Seq( "-deprecation", "-feature" ),
			scmInfo := Some(
				ScmInfo(
					url( "https://github.com/taig/parcelable" ),
					"scm:git:git://github.com/taig/parcelable.git",
					Some( "scm:git:git@github.com:taig/parcelable.git" )
				)
			),
			sourceGenerators in Compile <<= ( sourceGenerators in Compile ) ( generators => Seq( generators.last ) ),
			startYear := Some( 2015 ),
			version := "1.2.4",
			minSdkVersion in Android := "4",
			platformTarget in Android := "android-21",
			typedResources in Android := false
		)

	val pom =
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
	}
}