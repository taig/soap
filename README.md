# Parcelable (***Scala on Android***)

[![Circle CI](https://circleci.com/gh/Taig/Parcelable/tree/develop.svg?style=svg)](https://circleci.com/gh/Taig/Parcelable/tree/develop)

Parcelable is Android's serialization tool for inter-process communication (IPC). The emphasis on performance is the prominent difference to the Java Serialization framework (which the developer is discouraged to use for this very reason). Unfortunately, Parcelable requires the developer to implement a vast portion of boilerplate code in order to work. This project combines the performance of Parcelable with the ease of Java's Serializable interface.

## Installation

````scala
libraryDependencies ++= Seq(
  "io.taig.android" %% "parcelable" % "3.0.0-SNAPSHOT"
)
````

## Usage

TODO

## License

MIT, see [LICENSE][1] file for more information

[1]: https://raw.githubusercontent.com/Taig/Parcelable/master/LICENSE