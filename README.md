# Parcelable (***Scala on Android***)

[![Circle CI](https://img.shields.io/circleci/project/Taig/Parcelable/master.svg)](https://circleci.com/gh/Taig/Parcelable/tree/develop)
[![codecov.io](https://codecov.io/github/Taig/Parcelable/coverage.svg?branch=develop)](https://codecov.io/github/Taig/Parcelable?branch=develop)
[![Maven](https://img.shields.io/maven-central/v/io.taig.android/parcelable_2.11.svg)](http://search.maven.org/#artifactdetails%7Cio.taig.android%7Cparcelable_2.11%7C2.4.1%7Caar)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/Taig/Parcelable/develop/LICENSE)

Parcelable is Android's serialization tool for inter-process communication (IPC). The emphasis on performance is the prominent difference to the Java Serialization framework (which the developer is discouraged to use for this very reason). Unfortunately, Parcelable requires the developer to implement a vast portion of boilerplate code in order to work. This project combines the performance of Parcelable with the ease of Java's Serializable interface.

## Installation

````scala
libraryDependencies += "io.taig.android" %% "parcelable" % "3.0.0-SNAPSHOT"
````

## Overview

````scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

import io.taig.android.parcelable._, codecs._

sealed trait Animal
case class Cat( moody: Boolean ) extends Animal
case class Dog( name: String, age: Option[Int] ) extends Animal

// Exiting paste mode, now interpreting.

import io.taig.android.parcelable._
import codecs._
defined trait Animal
defined class Cat
defined class Dog

> Bundle( "my_cat", Cat( moody = true ) )
res0: Bundle = Bundle[{my_cat=Bundle[{moody=true}]}]

> Bundle( "some_dogs", Seq( Dog( Some( "Holly", Some( 2 ) ) ), Dog( "Freddy", None ) ) )
res1: Bundle = Bundle[{some_dogs=Bundle[{0=Bundle[{age=2, name=Holly}], 1=Bundle[{name=Freddy}]}]}]
````

## Usage

TODO

## Acknowledgements

This library is highly inspired by [travisbrown/circe][2], especially for the serialization of case classes and ADTs as well as the integration of [export-hook][3].

## License

MIT, see [LICENSE][1] file for more information

[1]: https://raw.githubusercontent.com/Taig/Parcelable/master/LICENSE
[2]: https://github.com/travisbrown/circe/
[3]: https://github.com/milessabin/export-hook/