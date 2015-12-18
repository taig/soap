import Settings._

lazy val parcelable = ( project in file( "." ) )
    .settings( common ++ noPublish: _* )
    .aggregate( core )

lazy val core = ( project in file( "core" ) )
    .settings( androidBuildAar ++ common ++ android: _* )
    .settings(
        fork in Test := true,
        libraryDependencies ++=
            compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full) ::
            "com.chuusai" %% "shapeless" % "2.2.5" ::
            "org.julienrf" %% "enum" % "1.1" ::
            "org.scala-lang" % "scala-reflect" % scalaVersion.value ::
            "com.geteit" %% "robotest" % "0.12" % "test" ::
            "org.scalatest" %% "scalatest" % "2.2.5" % "test" ::
            Nil,
        minSdkVersion := "4",
        name := "Parcelable",
        testOptions in Test += Tests.Argument( "-oDF" )
)