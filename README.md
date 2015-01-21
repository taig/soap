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
  
  override def writeToParcel( destination: Parcel, flags: Int )
  {
    destination.writeString( name )
    destination.writeInt( age )
  }
}

object Person extends com.taig.android.parcelable.Creator[Person]
{
  override lazy val CREATOR = new android.os.Parcelable.Creator[Person]
  {
    override def createFromParcel( source: android.os.Parcel ) = new Person(
      source.readString(),
      source.readInt()
    )

    override def newArray( size: Int ) = new Array[Person]( size )
  }
}
````

## Supported Types

- Bundle
- Boolean
- Byte
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

## Installation

Tested with sbt & [pfn/android-sdk-plugin][1]

````scala
resolvers += Resolver.url( "Taig", url( "http://taig.github.io/repository" ) )( ivyStylePatterns )

libraryDependencies ++= Seq(
  compilerPlugin( "org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full ),
  "com.taig.android" %% "parcelable" % "1.1.0"
)
````

## Usage

Using the library basically boils down to annotating classes or traits with the `@com.taig.android.parcelable.annotation.Parcelable` annotation. For more control one can implement the `android.os.Parcelable` interface on the class, or the `com.taig.android.parcelable.Creator[_]` trait on the companion object, in order to disable code generation and providing an own implementation instead.

### Basics

The Parcelable annotation is easy to use on any class or case class. During compile time the library will analyze your constructor fields and use them to generate appropiate Parcel write and read methods. There will be no runtime reflection code insterted to your classes; everything is evaluated to static method calls.

````scala
@Parcelable
class Address( val street: String, val zip: Int, val city: String )

@Parcelable
case class Company( name: String, employees: List[Person], offices: Map[Int, Address] )
````

````scala
class Address( val street: String, val zip: Int, val city: String ) extends android.os.Parcelable
{
  override def describeContents() = 0
  
  override def writeToParcel( destination: Parcel, flags: Int )
  {
    destination.writeString( street )
    destination.writeInt( zip )
    destination.writeString( city )
  }
}

object Address extends com.taig.android.parcelable.Creator[Address]
{
  override lazy val CREATOR = new android.os.Parcelable.Creator[Address]
  {
    override def createFromParcel( source: android.os.Parcel ) = new Address(
      source.readString(),
      source.readInt(),
      source.readString()
    )

    override def newArray( size: Int ) = new Array[Address]( size )
  }
}

case class Company( name: String, employees: List[Person], offices: Map[Int, Address] ) extends android.os.Parcelable
{
  override def describeContents() = 0
  
  override def writeToParcel( destination: Parcel, flags: Int )
  {
    destination.writeString( name )
    destination.writeStringArray( employees.toArray )
    destination.writeIntArray( offices.key.toArray )
    destination.writeParcelableArray( offices.values.toArray )
  }
}

object Company extends com.taig.android.parcelable.Creator[Company]
{
  override lazy val CREATOR = new android.os.Parcelable.Creator[Company]
  {
    override def createFromParcel( source: android.os.Parcel ) = new Company(
      source.readString(),
      source.createStringArray().to[List],
      {
        val keys = source.createIntArray()
        val values = source
                      .readParcelableArray( classOf[Address].getClassLoader )
                      .map( _.asInstanceOf[Address] )

        ( keys zip values ).toMap
      }
    )

    override def newArray( size: Int ) = new Array[Company]( size )
  }
}
````

### Inheritance

You can also annotate abstarct super types or traits with the Parcelable annotation. They will extend the `android.os.Parcelable` interface, but will not implement the methods. Scala requires a companion object with a `CREATOR` field whenever a class implements Parcelable. Therefore a dummy `CREATOR` is generated. All subclasses that you want to be Parcelable need to be annotated as well!

````scala
@Parcelable
trait Value

@Parcelable
case class Absolute( value: Int ) extends Value

@Parcelable
case class Relative( value: Float ) extends Value
````

````scala
trait Value extends android.os.Parcelable

object Value extends com.taig.android.parcelable.Creator[Value]
{
  def CREATOR: android.os.Parcelable.Creator[Value] = sys.error(
	  "Can not create an abstract type from parcel. Did you forget to annotate a child class?"
	)
}

case class Absolute( value: Int ) extends Value with android.os.Parcelable
{
  override def describeContents() = 0
  
  override def writeToParcel( destination: Parcel, flags: Int )
  {
    destination.writeInt( value )
  }
}

object Absolute extends com.taig.android.parcelable.Creator[Absolute]
{
  override lazy val CREATOR = new android.os.Parcelable.Creator[Absolute]
  {
    override def createFromParcel( source: android.os.Parcel ) = new Absolute( source.readInt() )

    override def newArray( size: Int ) = new Array[Absolute]( size )
  }
}

case class Relative( value: Float ) extends Value with android.os.Parcelable
...
````

### Singletons

It is possible to anntotate singleton objects as well. But they have to live on their own, it won't work if an actual class is around where the object is the companion. In the above `Value` example this would be a valid usage:

````scala
@Parcelable
trait Value

@Parcelable
object Auto extends Value
````

````scala
...

class Auto extends Value with android.os.Parcelable
{
  override def describeContents() = 0
  
  override def writeToParcel( destination: Parcel, flags: Int ) {}
}

object Auto extends Auto with com.taig.parcelable.Creator[Auto]
{
  override lazy val CREATOR = new android.os.Parcelable.Creator[Auto]
  {
    override def createFromParcel( source: android.os.Parcel ) = Auto

    override def newArray( size: Int ) = new Array[Auto]( size )
  }
}
````

## Changelog

#### 1.1.0
- Allow annotating `object`
- Allow annotating abstract classes and trais with type arguments
- Print a notice when `writeSerializable` is used, as this may not be intended

## Unsupported Parcel Feautes

- `writeException` / `readException`
- `writeInterfaceToken`
- `writeSparseArray` / `readSparseArray`

## Known limitations / issues

- IntelliJ does not support macro expansion yet, be prepared for red code
- Same file class declarations can break things, due to [scope issues][2]
- Subclassing supported generic types `Traversable[_]` and `Map[_, _]` can get you into trouble

[1]: https://github.com/pfn/android-sdk-plugin
[2]: https://github.com/scalamacros/paradise/issues/14
