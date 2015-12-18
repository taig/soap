import Settings._

lazy val parcelable = ( project in file( "." ) )
    .settings( common ++ noPublish: _* )
    .aggregate( core )

lazy val core = ( project in file( "core" ) )
    .settings( androidBuildAar ++ common ++ android: _* )
    .settings(
        addCompilerPlugin( "org.spire-math" %% "kind-projector" % "0.7.1" ),
        fork in Test := true,
        libraryDependencies ++=
            "com.chuusai" %% "shapeless" % "2.2.5" ::
            "org.julienrf" %% "enum" % "1.1" ::
            "com.geteit" %% "robotest" % "0.12" % "test" ::
            "org.scalatest" %% "scalatest" % "2.2.5" % "test" ::
            Nil,
        minSdkVersion := "4",
        name := "Parcelable",
        testOptions in Test += Tests.Argument( "-oDF" )
)