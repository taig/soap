# Soap

> *Scala on Android Parcelable* with [circe][1]

[![Circle CI](https://circleci.com/gh/Taig/soap/tree/master.svg?style=shield)](https://circleci.com/gh/Taig/soap/tree/master)
[![codecov](https://codecov.io/gh/Taig/Soap/branch/master/graph/badge.svg)](https://codecov.io/gh/Taig/Soap)
[![Maven](https://img.shields.io/maven-central/v/io.taig.android/soap_2.11.svg)](http://search.maven.org/#artifactdetails%7Cio.taig.android%7Csoap_2.11%7C4.0.2%7Caar)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/Taig/soap/master/LICENSE)

## Introduction

As of version 4.0.0, *Soap* is no more than a simple wrapper around [circe][1] (a Scala JSON library). It allows to easily read/write from/to `Bundle`, `Intent` and `SharedPreference` as long as the appropriate circe codecs are in scope.

In previous versions of *Soap*, the library provided its own codec generation framework. As I spent more and more time working with *circe*, I realised how superior its codec derivation is and that I will not be able to provide and maintain anything nearly as good. I therefore decided to migrate *Soap* to *circe*. It might not be the fastest or memory optimized way of solving inter-process-communication (IPC), but it does provide the greatest development experience.

> **Warning**  
> You can only use *Soap* to read data, if it has also been written with *Soap*. Reading form external Bundles (e.g. from a Notification) does not work, because the format differs.

## Installation

```scala
libraryDependencies += "io.taig.android" %% "soap" % "4.1.0"
```

## Quickstart

```scala
import io.taig.android.soap.implicits._

import io.taig.android.soap.Bundle

val bundle = Bundle( 2 )
    .write( "foo", 42 )
    .write( "bar", "foobar" )

bunlde.read[Int]( "foo" )           // Some( 42 )
bunlde.read[String]( "bar" )        // Some( "foobar" )
bunlde.read[Int]( "bar" )           // None
bunlde.read[String]( "foobar" )     // None

import android.content.Intent

val intent = new Intent()
    .write( "foo", 42 )
    .write( "bar", "foobar" )

intent.read[Int]( "foo" )           // Some( 42 )

import android.preference.PreferenceManager

val preferences = PreferenceManager.getDefaultSharedPreferences( ??? )
    .write( "foo", 42 )
    .write( "bar", "foobar" )

preferences.read[Int]( "foo" )      // Some( 42 )
```

See the [circe documentation][2] to find out more about codec generation.

[1]: https://github.com/travisbrown/circe/
[2]: https://travisbrown.github.io/circe/