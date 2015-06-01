# Parcelable (***Scala on Android***)

Parcelable is Android's serialization tool for inter-process communication (IPC). The emphasis on performance is the prominent difference to the Java Serialization framework (which the developer is discouraged to use for this very reason). Unfortunately, Parcelable requires the developer to implement a vast portion of boilerplate code in order to work. This project combines the performance of Parcelable with the ease of Java's Serializable interface.

With the help of macros, the project will expand all your `@Parcelable` annotated classes and traits in this fashion:

````scala
@Parcelable
case class Person( name: String, age: Int )
````

````scala
case class Person( name: String, age: Int ) extends android.os.Parcelable
{
  override def describeContents() = 0
  
  override def writeToParcel( destination: Parcel, flags: Int ) = ...
}

object Person extends io.taig.android.parcelable.Creator[Person]
{
  override lazy val CREATOR = new android.os.Parcelable.Creator[Person]
  {
    override def createFromParcel( source: android.os.Parcel ) = Person( ... )

    override def newArray( size: Int ) = new Array[Person]( size )
  }
}
````

## Installation

````scala
libraryDependencies ++= Seq(
  compilerPlugin( "org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full ),
  "io.taig.android" %% "parcelable" % "2.0.0-SNAPSHOT"
)
````

## Supported Types

- Bundle
- Boolean
- Byte
- Char
- CharSequence
- Double
- IBinder
- FileDescriptor
- Float
- Int
- Long
- Parcelable
- PersistableBundle
- Serializable
- Short
- Size
- SizeF
- String
- SparseBooleanArray
- Array[_]
- Traversable[_]
- Map[_, _]
- Option[_]
- Tuples

## Usage

Using the library basically boils down to annotating classes or traits with the `@io.taig.android.Parcelable` annotation. For more control one can implement the `android.os.Parcelable` interface on the class, or the `io.taig.android.parcelable.Creator[_]` trait on the companion object, in order to disable code generation and providing an own implementation instead.

### Basics

The Parcelable annotation is easy to use on any class or case class. During compile time the library will analyze your constructor fields and use them to generate appropiate Parcel write and read methods. For every supported type there is an implicit `Transformer` implementation available that knows how to parcel and unparcel an object of that type.

To add support for another type, all you have to do is implement a `Transformer` and have it in implicit scope.

### Inheritance

You can also annotate abstract super types or traits with the Parcelable annotation. They will extend the `android.os.Parcelable` interface, but will not implement the methods. Scala requires a companion object with a `CREATOR` field whenever a class implements Parcelable. Therefore a dummy `CREATOR` is generated. All subclasses that you want to be Parcelable need to be annotated as well!

````scala
@Parcelable
trait Value

@Parcelable
case class Absolute( value: Int ) extends Value

@Parcelable
case class Relative( value: Float ) extends Value
````

### Singletons

It is possible to anntotate singleton objects as well. But they have to live on their own, it won't work if the object is a class companion. In the above `Value` example this would be a valid usage:

````scala
@Parcelable
trait Value

@Parcelable
object Auto extends Value
````

## Changelog

#### 2.0.0

- Switched to a type class approach, making the parcel/unparcel process much more accessible and also easier to modify and improve

#### 1.2.6

- Upgrade to sbt 0.13.8
- Upgrade to android-sdk-plugin 1.3.23

#### 1.2.5

- Upgrade to Scala 2.11.6 & pfn/android 1.3.18
- Fix aar package name, making it not resolvable via maven

#### 1.2.4

- Change groupId to `io.taig.android`
- Publish project via Maven Central

#### 1.2.3

- Resolved match error for `Array[_ <: Parcelable]`

#### 1.2.2

- Resolved NPE issues with non primitive Option values, such as collections or tuples

#### 1.2.1

- Support for constructor argument groups

#### 1.1.1

- Only print Serializable warning, when the concerned type does not inherit from Serializable directly

#### 1.1.0

- Allow annotating `object`
- Allow annotating abstract classes and trais with type arguments
- Print a notice when `writeSerializable` is used, as this may not be intended

## Unsupported Parcel Features

- `writeException` / `readException`
- `writeInterfaceToken`
- `writeSparseArray` / `readSparseArray`

## Known Limitations / Issues

- IntelliJ does not support macro expansion yet, be prepared for red code
- Same file class declarations can break things, due to [scope issues][2]

## License

MIT, see [LICENSE][3] file for more information

[1]: https://github.com/pfn/android-sdk-plugin
[2]: https://github.com/scalamacros/paradise/issues/14
[3]: https://raw.githubusercontent.com/Taig/Parcelable/master/LICENSE
