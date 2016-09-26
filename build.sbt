lazy val soap = project.in( file( "." ) )
    .settings( Settings.common ++ Settings.noPublish )
    .aggregate( core )

lazy val core = project
    .settings( androidBuildAar ++ Settings.common ++ Settings.android )
    .settings(
        addCompilerPlugin( "org.spire-math" %% "kind-projector" % "0.8.0" ),
        fork in Test := true,
        libraryDependencies ++=
            "com.chuusai" %% "shapeless" % "2.3.2" ::
            "org.typelevel" %% "cats-core" % "0.7.2" ::
            "org.typelevel" %% "cats-macros" % "0.7.2" ::
            "org.julienrf" %% "enum" % "3.0" ::
            "com.geteit" %% "robotest" % "0.12" % "test" ::
            "org.scalatest" %% "scalatest" % "3.0.0" % "test" ::
            Nil,
        name := "Soap",
        testOptions in Test += Tests.Argument( "-oDF" )
)