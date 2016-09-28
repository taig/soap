# Changelog

## 4.0.0-RC1

_2016-09-28_

 * Removed entire codec infrastructure and migrated to *circe*.
 * Removed multi-project build definition since only one project is used

## 3.0.5

_2016-08-22_

 * Upgrade to shapeless 2.3.2
 * Upgrade to cats 0.7.0
 * Upgrade to scalatest 3.0.0
 * Upgrade to sbt-android 1.6.13
 * Upgrade to sbt 0.13.12

## 3.0.4

_2016-07-15_

 * Upgrade to android platform 24
 * Upgrade to cats 0.6.1
 * Upgrade to shapeless 2.3.1
 * Upgrade to sbt-sonatype 1.1.0
 * Upgrade to sbt-scalariform 1.7.0
 * Upgrade to sbt-android 1.6.7
 * Upgrade to kind-projector 0.8.0

## 3.0.3

_2016-05-30_

 * Upgrade to cats 0.6.0
 * Upgrade to sbt-android 1.6.3

## 3.0.2

 * Upgrade to sbt-android 1.6.0
 * Upgrade to julienrf/enum 3.0
 * Upgrade to cats 0.5.0

## 3.0.1

 * Upgrade to Scala 2.11.8

##  3.0.0

 * Type class restructuring

## 3.0.0-BETA3

 * Integrate cats

## 3.0.0-BETA2

 * Renamed project to Soap (Scala on Android Parcelable)
 * Added io.taig.android.soap.implicits object

## 3.0.0-BETA1

 * Complete shapeless-based rewrite, ditching the `@Parcelable`-Annotation in favor of Bundle-based serialization

## 2.4.0

 * Added read/write methods for Bundles and Intents

##  2.3.0

 * Added Try[_] support (exceptions are serialized via runtime reflection)

## 2.2.0

 * Added Either[Left, Right] support

## 2.1.1

 * Added URL support

## 2.1.0

 * Added Enumeration support (runtime reflection)

## 2.0.0

 * Switched to a type class approach, making the parcel/unparcel process much more accessible and also easier to modify and improve

## 1.2.6

 * Upgrade to sbt 0.13.8
 * Upgrade to android-sdk-plugin 1.3.23

## 1.2.5

 * Upgrade to Scala 2.11.6 & pfn/android 1.3.18
 * Fix aar package name, making it not resolvable via maven

## 1.2.4

 * Change groupId to `io.taig.android`
 * Publish project via Maven Central

## 1.2.3

 * Resolved match error for `Array[_ <: Parcelable]`

## 1.2.2

 * Resolved NPE issues with non primitive Option values, such as collections or tuples

## 1.2.1

 * Support for constructor argument groups

## 1.1.1

 * Only print Serializable warning, when the concerned type does not inherit from Serializable directly

## 1.1.0

 * Allow annotating `object`
 * Allow annotating abstract classes and trais with type arguments
 * Print a notice when `writeSerializable` is used, as this may not be intended
