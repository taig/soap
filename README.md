# Parcelable (***Scala on Android***)

[![Circle CI](https://circleci.com/gh/Taig/Parcelable/tree/develop.svg?style=svg)](https://circleci.com/gh/Taig/Parcelable/tree/develop)
[![codecov.io](https://codecov.io/github/Taig/Parcelable/coverage.svg?branch=develop)](https://codecov.io/github/Taig/Parcelable?branch=develop)

Parcelable is Android's serialization tool for inter-process communication (IPC). The emphasis on performance is the prominent difference to the Java Serialization framework (which the developer is discouraged to use for this very reason). Unfortunately, Parcelable requires the developer to implement a vast portion of boilerplate code in order to work. This project combines the performance of Parcelable with the ease of Java's Serializable interface.

## Installation

````scala
libraryDependencies ++= Seq(
  "io.taig.android" %% "parcelable" % "3.0.0-SNAPSHOT"
)
````

## Usage

TODO

## Acknowledgements

This library is highly inspired by [travisbrown/circe][2], especially for the serialization of case classes and ADTs.

## License

MIT, see [LICENSE][1] file for more information

[1]: https://raw.githubusercontent.com/Taig/Parcelable/master/LICENSE
[2]: https://github.com/travisbrown/circe
