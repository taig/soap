import android.Keys._
import sbt.Keys._
import sbt._
import xerial.sbt.Sonatype.autoImport._

object Settings {
    val android = Seq(
        platformTarget := "android-23",
        typedResources := false
    )

    val common = Seq(
        javacOptions ++=
            "-source" :: "1.7" ::
            "-target" :: "1.7" ::
            Nil,
        organization := "io.taig.android",
        scalacOptions ++=
            "-deprecation" ::
            "-feature" ::
            Nil,
        scalaVersion := "2.11.7",
        version := "3.0.0-BETA1"
    )

    val noPublish = Seq(
        publish := (),
        publishLocal := (),
        publishArtifact := false
    )
    
    val sonatype = Seq(
        description := "Zero boilerplate compile time code generation for Android serialization",
        homepage := Some( url( "https://github.com/taig/parcelable" ) ),
        licenses := Seq( "MIT" -> url( "https://raw.githubusercontent.com/taig/parcelable/master/LICENSE" ) ),
        organizationHomepage := Some( url( "http://taig.io" ) ),
        pomExtra := {
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
        publishArtifact := false,
        publishMavenStyle := true,
        publishTo <<= version ( version => {
            val url = Some( "https://oss.sonatype.org/" )
    
            if( version.endsWith( "SNAPSHOT" ) ) {
                url.map( "snapshot" at _ + "content/repositories/snapshots" )
            } else {
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
        sonatypeProfileName := "io.taig",
        startYear := Some( 2015 )
    )
}