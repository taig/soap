# Soap

> Scala on Android Parcelable

[![Circle CI](https://img.shields.io/circleci/project/Taig/Soap/master.svg)](https://circleci.com/gh/Taig/Soap/tree/master)
[![codecov.io](https://codecov.io/github/Taig/Soap/coverage.svg?branch=master)](https://codecov.io/github/Taig/Soap?branch=master)
[![Maven](https://img.shields.io/maven-central/v/io.taig.android/soap_2.11.svg)](http://search.maven.org/#artifactdetails%7Cio.taig.android%7Csoap_2.11%7C3.0.0%7CBETA3%7Caar)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/Taig/Soap/master/LICENSE)

Parcelable is Android's serialization tool for inter-process communication (IPC). The emphasis on performance is the prominent difference to the Java Serialization framework (which the developer is discouraged to use for this very reason). Unfortunately, Parcelable requires the developer to implement a vast portion of boilerplate code in order to work. This project combines the performance of Parcelable with the ease of Java's Serializable interface.

## Installation

````scala
libraryDependencies += "io.taig.android" %% "soap" % "3.0.0-SNAPSHOT"
````

## Overview

### Basic usage

TODO

### Activity / Intent

````scala
import io.taig.android.soap.implicits._

class MyActivity extends Activity {
    lazy val amount: Option[Int] = getIntent.read[Int]( "amount" )

    var myStateValue: Option[String] = None

    override def onCreate( state: Bundle ): Unit = {
        super.onCreate( state )

        myStateValue = Option( state ).flatMap( _.read[String]( "my-state-value" ) )
    }

    ...

    override def onSaveInstanceState( state: Bundle ): Unit = {
        super.onSaveInstanceState( state )

        state.write( "my-state-value", myStateValue )
    }
}

object MyActivity {
    def apply( amount: Int ): Intent = {
        new Intent( classOf[MyActivity] ).write( "amount", amount )
    }
}
````

### Fragments

TODO

## Adding support for custom types

TODO

## A word on `Serializable`

TODO

## Acknowledgements

This library is highly inspired by [travisbrown/circe][2], especially for the serialization of case classes and ADTs.

## License

MIT, see [LICENSE][1] file for more information

[1]: https://raw.githubusercontent.com/Taig/Soap/master/LICENSE
[2]: https://github.com/travisbrown/circe/
