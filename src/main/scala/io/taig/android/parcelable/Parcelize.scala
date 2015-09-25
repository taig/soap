package io.taig.android.parcelable

import java.io.FileDescriptor
import java.net.URL

import android.annotation.TargetApi
import android.os._
import android.text.TextUtils
import android.util.{ Size, SizeF, SparseBooleanArray }

import scala.collection.breakOut
import scala.collection.generic.CanBuildFrom
import scala.language.{ higherKinds, reflectiveCalls }
import scala.reflect._

/**
 * Instructions on how to parcel and unparcel an object of type T
 */
trait Parcelize[T] {
    def read( source: Parcel ): T

    def write( value: T, destination: Parcel, flags: Int ): Unit
}

object Parcelize extends TupleParcelize {
    implicit val `Parcelize[Bundle]` = new Parcelize[Bundle] {
        override def read( source: Parcel ) = source.readBundle()

        override def write( value: Bundle, destination: Parcel, flags: Int ) = destination.writeBundle( value )
    }

    implicit val `Parcelize[Boolean]` = new Parcelize[Boolean] {
        override def read( source: Parcel ) = source.readValue( classOf[Boolean].getClassLoader ).asInstanceOf[Boolean]

        override def write( value: Boolean, destination: Parcel, flags: Int ) = destination.writeValue( value )
    }

    implicit val `Parcelize[Byte]` = new Parcelize[Byte] {
        override def read( source: Parcel ) = source.readByte()

        override def write( value: Byte, destination: Parcel, flags: Int ) = destination.writeByte( value )
    }

    implicit val `Parcelize[Char]` = new Parcelize[Char] {
        override def read( source: Parcel ) = source.readInt().toChar

        override def write( value: Char, destination: Parcel, flags: Int ) = destination.writeInt( value.toInt )
    }

    implicit val `Parcelize[CharSequence]` = new Parcelize[CharSequence] {
        override def read( source: Parcel ) = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel( source )

        override def write( value: CharSequence, destination: Parcel, flags: Int ) = {
            TextUtils.writeToParcel( value, destination, flags )
        }
    }

    implicit val `Parcelize[Double]` = new Parcelize[Double] {
        override def read( source: Parcel ) = source.readDouble()

        override def write( value: Double, destination: Parcel, flags: Int ) = destination.writeDouble( value )
    }

    implicit val `Parcelize[FileDescriptor]` = new Parcelize[FileDescriptor] {
        override def read( source: Parcel ) = source.readFileDescriptor().getFileDescriptor

        override def write( value: FileDescriptor, destination: Parcel, flags: Int ) = destination.writeFileDescriptor( value )
    }

    implicit val `Parcelize[Float]` = new Parcelize[Float] {
        override def read( source: Parcel ) = source.readFloat()

        override def write( value: Float, destination: Parcel, flags: Int ) = destination.writeFloat( value )
    }

    implicit val `Parcelize[IBinder]` = new Parcelize[IBinder] {
        override def read( source: Parcel ) = source.readStrongBinder()

        override def write( value: IBinder, destination: Parcel, flags: Int ) = destination.writeStrongBinder( value )
    }

    implicit val `Parcelize[Int]` = new Parcelize[Int] {
        override def read( source: Parcel ) = source.readInt()

        override def write( value: Int, destination: Parcel, flags: Int ) = destination.writeInt( value )
    }

    implicit val `Parcelize[Long]` = new Parcelize[Long] {
        override def read( source: Parcel ) = source.readInt()

        override def write( value: Long, destination: Parcel, flags: Int ) = destination.writeLong( value )
    }

    implicit val `Parcelize[PersistableBundle]` = new Parcelize[PersistableBundle] {
        @TargetApi( 21 )
        override def read( source: Parcel ) = source.readPersistableBundle()

        @TargetApi( 21 )
        override def write( value: PersistableBundle, destination: Parcel, flags: Int ) = {
            destination.writePersistableBundle( value )
        }
    }

    implicit val `Parcelize[Short]` = new Parcelize[Short] {
        override def read( source: Parcel ) = source.readValue( classOf[Short].getClassLoader ).asInstanceOf[Short]

        override def write( value: Short, destination: Parcel, flags: Int ) = destination.writeValue( value )
    }

    implicit val `Parcelize[Size]` = new Parcelize[Size] {
        @TargetApi( 21 )
        override def read( source: Parcel ) = source.readSize()

        @TargetApi( 21 )
        override def write( value: Size, destination: Parcel, flags: Int ) = destination.writeSize( value )
    }

    implicit val `Parcelize[SizeF]` = new Parcelize[SizeF] {
        @TargetApi( 21 )
        override def read( source: Parcel ) = source.readSizeF()

        @TargetApi( 21 )
        override def write( value: SizeF, destination: Parcel, flags: Int ) = destination.writeSizeF( value )
    }

    implicit val `Parcelize[SparceBooleanArray]` = new Parcelize[SparseBooleanArray] {
        override def read( source: Parcel ) = source.readSparseBooleanArray()

        override def write( value: SparseBooleanArray, destination: Parcel, flags: Int ) = {
            destination.writeSparseBooleanArray( value )
        }
    }

    implicit val `Parcelize[String]` = new Parcelize[String] {
        override def read( source: Parcel ) = source.readString()

        override def write( value: String, destination: Parcel, flags: Int ) = destination.writeString( value )
    }

    implicit val `Parcelize[URL]` = new Parcelize[URL] {
        override def read( source: Parcel ) = new URL( source.readString() )

        override def write( value: URL, destination: Parcel, flags: Int ) = {
            destination.writeString( value.toExternalForm )
        }
    }

    implicit def `Parcelize[Either]`[L: Parcelize, R: Parcelize] = new Parcelize[Either[L, R]] {
        val left = implicitly[Parcelize[L]]

        val right = implicitly[Parcelize[R]]

        override def read( source: Parcel ) = source.readInt() match {
            case 0 ⇒ Left( left.read( source ) )
            case 1 ⇒ Right( right.read( source ) )
        }

        override def write( value: Either[L, R], destination: Parcel, flags: Int ) = value match {
            case Left( value ) ⇒
                destination.writeInt( 0 )
                left.write( value, destination, flags )
            case Right( value ) ⇒
                destination.writeInt( 1 )
                right.write( value, destination, flags )
        }
    }

    implicit def `Parcelize[Option]`[T: Parcelize] = new Parcelize[Option[T]] {
        val parcelize = implicitly[Parcelize[T]]

        override def read( source: Parcel ) = source.readInt() match {
            case 1  ⇒ Some( parcelize.read( source ) )
            case -1 ⇒ None
        }

        override def write( value: Option[T], destination: Parcel, flags: Int ) = value match {
            case Some( value ) ⇒
                destination.writeInt( 1 )
                parcelize.write( value, destination, flags )
            case None ⇒ destination.writeInt( -1 )
        }
    }

    implicit def `Parcelize[Parcelable]`[T <: Parcelable: ClassTag] = new Parcelize[T] {
        override def read( source: Parcel ) = {
            source.readParcelable[T]( classTag[T].runtimeClass.getClassLoader )
        }

        override def write( value: T, destination: Parcel, flags: Int ) = destination.writeParcelable( value, flags )
    }

    implicit def `Parcelize[Array]`[T: Parcelize]( implicit tag: ClassTag[T] ) = new Parcelize[Array[T]] {
        val parcelize = implicitly[Parcelize[T]]

        override def read( source: Parcel ) = {
            ( 0 until source.readInt() ).map( _ ⇒ parcelize.read( source ) ).toArray
        }

        override def write( value: Array[T], destination: Parcel, flags: Int ) = {
            destination.writeInt( value.size )
            value.foreach( parcelize.write( _, destination, flags ) )
        }
    }

    implicit def `Parcelize[Traversable]`[L[B] <: Traversable[B], T: Parcelize]( implicit cbf: CanBuildFrom[Nothing, T, L[T]] ) = new Parcelize[L[T]] {
        val parcelize = implicitly[Parcelize[T]]

        override def read( source: Parcel ) = {
            ( 0 until source.readInt() ).map( _ ⇒ parcelize.read( source ) )( breakOut )
        }

        override def write( value: L[T], destination: Parcel, flags: Int ) = {
            destination.writeInt( value.size )
            value.foreach( parcelize.write( _, destination, flags ) )
        }
    }

    implicit def `Parcelize[Map]`[M[A, B] <: Map[A, B], S: Parcelize, T: Parcelize]( implicit cbf: CanBuildFrom[Nothing, ( S, T ), M[S, T]] ) = new Parcelize[M[S, T]] {
        val parcelize = new {
            val key = implicitly[Parcelize[S]]

            val value = implicitly[Parcelize[T]]
        }

        override def read( source: Parcel ) = {
            ( 0 until source.readInt() )
                .map( _ ⇒ parcelize.key.read( source ) )
                .map( ( _, parcelize.value.read( source ) ) )( breakOut )
        }

        override def write( value: M[S, T], destination: Parcel, flags: Int ) = {
            destination.writeInt( value.size )
            value.keys.foreach( parcelize.key.write( _, destination, flags ) )
            value.values.foreach( parcelize.value.write( _, destination, flags ) )
        }
    }
}