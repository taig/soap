lazy val soap = project.in( file( "." ) )
    .settings( Settings.common ++ Settings.noPublish )
    .aggregate( core )

lazy val core = project
    .settings( androidBuildAar ++ Settings.common ++ Settings.android )
    .settings(
        fork in Test := true,
        libraryDependencies ++=
            "io.circe" %% "circe-core" % "0.5.1" ::
            "io.circe" %% "circe-generic" % "0.5.1" ::
            "io.circe" %% "circe-parser" % "0.5.1" ::
            "com.geteit" %% "robotest" % "0.12" % "test" ::
            "org.scalatest" %% "scalatest" % "3.0.0" % "test" ::
            Nil,
        name := "soap",
        testOptions in Test += Tests.Argument( "-oDF" )
)