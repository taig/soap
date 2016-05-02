lazy val soap = ( project in file( "." ) )
    .settings( Settings.common ++ Settings.noPublish: _* )
    .aggregate( core )

lazy val core = project
    .settings( androidBuildAar ++ Settings.common ++ Settings.android: _* )
    .settings(
        addCompilerPlugin( "org.spire-math" %% "kind-projector" % "0.7.1" ),
        fork in Test := true,
        libraryDependencies ++=
            "com.chuusai" %% "shapeless" % "2.3.0" ::
            "org.typelevel" %% "cats-core" % "0.5.0" ::
            "org.typelevel" %% "cats-macros" % "0.5.0" ::
            "org.julienrf" %% "enum" % "3.0" ::
            "com.geteit" %% "robotest" % "0.12" % "test" ::
            "org.scalatest" %% "scalatest" % "3.0.0-M15" % "test" ::
            Nil,
        name := "Soap",
        minSdkVersion := "4",
        testOptions in Test += Tests.Argument( "-oDF" )
)