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

## Installation
Tested with sbt & [pfn/android-sdk-plugin][1]

````scala
libraryDependencies ++= Seq(
  compilerPlugin( "org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full ),
  "com.taig.android" % "parcelable" % "1.0.0"
)
````

## Usage
Todo

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

## Unsupported Parcel Feautes
- `writeException` / `readException`
- `writeInterfaceToken`
- `writeSparseArray` / `readSparseArray`

## Known limitations / issues
- IntelliJ does not support macro expansion yet, be prepared for red code

[1]: https://github.com/pfn/android-sdk-plugin
