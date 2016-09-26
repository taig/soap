# Soap

> Scala on Android Parcelable with [circe][1]

[![Circle CI](https://circleci.com/gh/Taig/soap/tree/master.svg?style=shield)](https://circleci.com/gh/Taig/soap/tree/master)
[![codecov](https://codecov.io/gh/Taig/Soap/branch/master/graph/badge.svg)](https://codecov.io/gh/Taig/Soap)
[![Maven](https://img.shields.io/maven-central/v/io.taig.android/soap_2.11.svg)](http://search.maven.org/#artifactdetails%7Cio.taig.android%7Csoap_2.11%7C4.0.0-SNAPSHOT%7Caar)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/Taig/soap/master/LICENSE)

Parcelable is Android's serialization tool for inter-process communication (IPC). The emphasis on performance is the prominent difference to the Java Serialization framework (which the developer is discouraged to use for this very reason). Unfortunately, Parcelable requires the developer to implement a vast portion of boilerplate code in order to work. This project combines the performance of Parcelable with the ease of Java's Serializable interface.

## Installation

````scala
libraryDependencies += "io.taig.android" %% "soap" % "4.0.0-SNAPSHOT"
````

[1]: https://github.com/travisbrown/circe/