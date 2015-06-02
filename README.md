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
  "io.taig.android" %% "parcelable" % "2.1.0"
)
````

## Supported Types

- Bundle
- **Boolean**
- Byte
- Char
- CharSequence
- Double
- **Enumeration**
- IBinder
- FileDescriptor
- Float
- Int
- Long
- Parcelable
- PersistableBundle
- Short
- Size
- SizeF
- String
- SparseBooleanArray
- **Array[_]**
- **Traversable[_]**
- **Map[_, _]**
- **Option[_]**
- **Tuples**

Supported types with generic arguments (e.g. Array[_]) work with every supported type (e.g. Array[Int] or Array[Option[( String, Int )]]).

The Serialization type is not supported and discouraged to use. You can still create a simple Serialization transformer though.

## Usage

Using the library basically boils down to annotating classes or traits with the `@io.taig.android.Parcelable` annotation. During compile time the library will analyze your constructor fields and use them to generate appropriate Parcel write and read methods. For every supported type there is an implicit `Transformer` implementation available that knows how to parcel and unparcel an object of that type.

To add support for custom types, all you have to do is implement a `Transformer` and have it in implicit scope. [See the source][1] on how to implement.

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

## Unsupported Parcel Features

- `writeException` / `readException`
- `writeInterfaceToken`
- `writeSparseArray` / `readSparseArray`

## Known Limitations / Issues

- IntelliJ does not support macro expansion yet, be prepared for red code
- Same file class declarations can break things, due to [scope issues][2]

## License

MIT, see [LICENSE][3] file for more information

[1]: https://github.com/Taig/Parcelable/blob/master/src/main/scala/io/taig/android/parcelable/Transformer.scala
[2]: https://github.com/scalamacros/paradise/issues/14
[3]: https://raw.githubusercontent.com/Taig/Parcelable/master/LICENSE