lazy val parcelable = ( project in file( "." ) )
    .settings( Settings.common ++ Settings.sonatype ++ Settings.noPublish: _* )
    .settings(
        name := "Parcelable"
    )
    .aggregate( core )

lazy val core = project
    .settings( androidBuildAar ++ Settings.common ++ Settings.sonatype ++ Settings.android: _* )
    .settings(
        fork in Test := true,
        libraryDependencies ++=
            "com.chuusai" %% "shapeless" % "2.2.5" ::
            "org.julienrf" %% "enum" % "2.1" ::
            "com.geteit" %% "robotest" % "0.12" % "test" ::
            "org.scalatest" %% "scalatest" % "2.2.6" % "test" ::
            Nil,
        minSdkVersion := "4",
        testOptions in Test += Tests.Argument( "-oDF" )
)