import Settings._

lazy val parcelable = project in file( "." ) settings( common ++ noPublish: _* ) aggregate( core, benchmark )

lazy val core = project in file( "core" ) settings( androidBuildAar ++ common: _* ) settings(
    fork in Test := true,
    libraryDependencies ++=
        "com.chuusai" %% "shapeless" % "2.2.5" ::
        "org.julienrf" %% "enum" % "1.1" ::
        "org.scala-lang" % "scala-reflect" % scalaVersion.value ::
        "com.geteit" %% "robotest" % "0.12" % "test" ::
        "org.scalatest" %% "scalatest" % "2.2.5" % "test" ::
        Nil,
    minSdkVersion := "4",
    name := "Parcelable",
    platformTarget := "android-23",
    targetSdkVersion := "23",
    testOptions in Test += Tests.Argument( "-oDF" ),
    typedResources := false
)

lazy val benchmark = ( project in file( "benchmark" ) )
    .settings( common: _* )
    .enablePlugins( JmhPlugin )
    .dependsOn( core )