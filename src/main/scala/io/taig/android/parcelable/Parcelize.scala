package io.taig.android.parcelable

import java.io.FileDescriptor
import java.net.URL

import android.annotation.TargetApi
import android.os._
import android.text.TextUtils
import android.util.{ Size, SizeF, SparseBooleanArray }

import scala.language.{ higherKinds, reflectiveCalls }
import scala.reflect._

/**
 * Instructions on how to parcel and unparcel an object of type T
 */
trait Parcelize[T] {
    def read( source: Parcel ): T

    def write( value: T, destination: Parcel, flags: Int ): Unit
}

object Parcelize extends TupleParcelize with TraversableParcelize {
    /*
     def apply[T]( r: ( Bundle, String ) ⇒ T, w: ( Bundle, String, T ) ⇒ Unit ): Bundleize[T] = new Bundleize[T] {
        override def read( key: String, bundle: Bundle ) = r( bundle, key )

        override def write( key: String, value: T, bundle: Bundle ) = w( bundle, key, value )
    }
     */
    def apply[T]( r: Parcel ⇒ T, w: Parcel ⇒ T ⇒ Unit ): Parcelize[T] = new Parcelize[T] {
        override def read( source: Parcel ) = r( source )

        override def write( value: T, destination: Parcel, flags: Int ) = w( destination )( value )
    }

    implicit val `Parcelize[Bundle]` = Parcelize[Bundle]( _.readBundle(), _.writeBundle )

    implicit val `Parcelize[Boolean]` = Parcelize[Boolean](
        _.readValue( classOf[Boolean].getClassLoader ).asInstanceOf[Boolean],
        _.writeValue
    )

    implicit val `Parcelize[Byte]` = Parcelize[Byte]( _.readByte(), _.writeByte )

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

    implicit val `Parcelize[Double]` = Parcelize[Double]( _.readDouble(), _.writeDouble )

    implicit val `Parcelize[FileDescriptor]` = Parcelize[FileDescriptor](
        _.readFileDescriptor().getFileDescriptor,
        _.writeFileDescriptor
    )

    implicit val `Parcelize[Float]` = Parcelize[Float]( _.readFloat(), _.writeFloat )

    implicit val `Parcelize[IBinder]` = Parcelize[IBinder]( _.readStrongBinder(), _.writeStrongBinder )

    implicit val `Parcelize[Int]` = Parcelize[Int]( _.readInt(), _.writeInt )

    implicit val `Parcelize[Long]` = Parcelize[Long]( _.readLong(), _.writeLong )

    implicit val `Parcelize[PersistableBundle]` = new Parcelize[PersistableBundle] {
        @TargetApi( 21 )
        override def read( source: Parcel ) = source.readPersistableBundle()

        @TargetApi( 21 )
        override def write( value: PersistableBundle, destination: Parcel, flags: Int ) = {
            destination.writePersistableBundle( value )
        }
    }

    implicit val `Parcelize[Short]` = Parcelize[Short](
        _.readValue( classOf[Short].getClassLoader ).asInstanceOf[Short],
        _.writeValue
    )

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

    implicit val `Parcelize[SparceBooleanArray]` = Parcelize[SparseBooleanArray](
        _.readSparseBooleanArray(),
        _.writeSparseBooleanArray
    )

    implicit val `Parcelize[String]` = Parcelize[String]( _.readString(), _.writeString )

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

    implicit def `Parcelize[Parcelable]`[T <: android.os.Parcelable: ClassTag] = new Parcelize[T] {
        override def read( source: Parcel ) = {
            source.readParcelable[T]( classTag[T].runtimeClass.getClassLoader )
        }

        override def write( value: T, destination: Parcel, flags: Int ) = destination.writeParcelable( value, flags )
    }

    implicit def `Parcelize[Array]`[T: Parcelize]( implicit tag: ClassTag[T] ) = new Parcelize[Array[T]] {
        override def read( source: Parcel ) = implicitly[Parcelize[Seq[T]]].read( source ).toArray

        override def write( value: Array[T], destination: Parcel, flags: Int ) = {
            implicitly[Parcelize[Seq[T]]].write( value.toSeq, destination, flags )
        }
    }
}