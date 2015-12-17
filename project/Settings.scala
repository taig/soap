import android.Keys._
import sbt.Keys._

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
        version := "3.0.0-SNAPSHOT"
    )

    val noPublish = Seq(
        publish := (),
        publishLocal := (),
        publishArtifact := false
    )
}