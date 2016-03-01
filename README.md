# Soap

> Scala on Android Parcelable

[![Circle CI](https://img.shields.io/circleci/project/Taig/Soap/master.svg)](https://circleci.com/gh/Taig/Soap/tree/master)
[![codecov.io](https://codecov.io/github/Taig/Soap/coverage.svg?branch=master)](https://codecov.io/github/Taig/Soap?branch=master)
[![Maven](https://img.shields.io/maven-central/v/io.taig.android/soap_2.11.svg)](http://search.maven.org/#artifactdetails%7Cio.taig.android%7Csoap_2.11%7C3.0.0%7CBETA3%7Caar)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/Taig/Soap/master/LICENSE)

Soap is Android's serialization tool for inter-process communication (IPC). The emphasis on performance is the prominent difference to the Java Serialization framework (which the developer is discouraged to use for this very reason). Unfortunately, Soap requires the developer to implement a vast portion of boilerplate code in order to work. This project combines the performance of Soap with the ease of Java's Serializable interface.

## Installation

````scala
libraryDependencies += "io.taig.android" %% "soap" % "3.0.0-BETA3"
````

## Overview

````scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

import io.taig.android.soap.Bundle
import io.taig.android.soap.implicits._

sealed trait Animal
case class Cat( moody: Boolean ) extends Animal
case class Dog( name: String, age: Option[Int] ) extends Animal

// Exiting paste mode, now interpreting.

import io.taig.android.soap.Bundle
import io.taig.android.soap.implicits._
defined trait Animal
defined class Cat
defined class Dog

> Bundle( "my_cat", Cat( moody = true ) )
res0: Bundle = Bundle[{my_cat=Bundle[{moody=true}]}]

> Bundle( "some_dogs", Seq( Dog( Some( "Holly", Some( 2 ) ) ), Dog( "Freddy", None ) ) )
res1: Bundle = Bundle[{some_dogs=Bundle[{0=Bundle[{age=2, name=Holly}], 1=Bundle[{name=Freddy}]}]}]

> res0.read[Cat]( "my_cat" )
res2: Cat = Cat(true)
````

## Acknowledgements

This library is highly inspired by [travisbrown/circe][2], especially for the serialization of case classes and ADTs.

## License

MIT, see [LICENSE][1] file for more information

[1]: https://raw.githubusercontent.com/Taig/Soap/master/LICENSE
[2]: https://github.com/travisbrown/circe/
