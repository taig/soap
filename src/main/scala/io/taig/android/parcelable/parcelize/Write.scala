package io.taig.android.parcelable.parcelize

import java.io.FileDescriptor
import java.net.URL

import android.annotation.TargetApi
import android.os.{ IBinder, Parcel, PersistableBundle }
import android.text.TextUtils
import android.util.{ Size, SizeF, SparseBooleanArray }
import io.taig.android.parcelable._

import scala.reflect.ClassTag

/**
 * Type class that instructs how to write a value to a given Parcel
 */
trait Write[-T] {
    def write( destination: Parcel, value: T, flags: Int ): Unit
}

trait Write1 {
    implicit def `Write[Bundleable]`[T: bundleable.Write]: Write[T] = Write { ( destination, value ) ⇒
        destination.writeBundle( implicitly[bundleable.Write[T]].write( value ) )
    }
}

trait Write0 extends Write1 {
    implicit val `Write[Array[Binder]`: Write[Array[IBinder]] = Write( _.writeBinderArray( _ ) )

    implicit val `Write[Array[Boolean]`: Write[Array[Boolean]] = Write( _.writeBooleanArray( _ ) )

    implicit val `Write[Array[Byte]`: Write[Array[Byte]] = Write( _.writeByteArray( _ ) )

    implicit val `Write[Array[Char]`: Write[Array[Char]] = Write( _.writeCharArray( _ ) )

    implicit val `Write[Array[Double]`: Write[Array[Double]] = Write( _.writeDoubleArray( _ ) )

    implicit val `Write[Array[Float]`: Write[Array[Float]] = Write( _.writeFloatArray( _ ) )

    implicit val `Write[Array[Int]`: Write[Array[Int]] = Write( _.writeIntArray( _ ) )

    implicit val `Write[Array[Long]`: Write[Array[Long]] = Write( _.writeLongArray( _ ) )

    implicit def `Write[Array[Parcelize]]`[T: Write]: Write[Array[T]] = Write { ( destination, values ) ⇒
        destination.write( values.length )
        values.foreach( destination.write( _ ) )
    }

    implicit val `Write[Array[String]`: Write[Array[String]] = Write( _.writeStringArray( _ ) )

    implicit val `Write[Bundle]`: Write[Bundle] = Write( _.writeBundle( _ ) )

    implicit val `Write[Boolean]`: Write[Boolean] = Write( _.writeValue( _ ) )

    implicit val `Write[Byte]`: Write[Byte] = Write( _.writeByte( _ ) )

    implicit val `Write[Char]`: Write[Char] = Write { ( destination, value ) ⇒
        destination.writeInt( value.toInt )
    }

    implicit val `Write[CharSequence]`: Write[CharSequence] = new Write[CharSequence] {
        override def write( destination: Parcel, value: CharSequence, flags: Int ) = {
            TextUtils.writeToParcel( value, destination, flags )
        }
    }

    implicit val `Write[Double]`: Write[Double] = Write[Double]( _.writeDouble( _ ) )

    implicit val `Write[FileDescriptor]`: Write[FileDescriptor] = Write( _.writeFileDescriptor( _ ) )

    implicit val `Write[Float]`: Write[Float] = Write( _.writeFloat( _ ) )

    implicit val `Write[IBinder]`: Write[IBinder] = Write( _.writeStrongBinder( _ ) )

    implicit val `Write[Int]`: Write[Int] = Write( _.writeInt( _ ) )

    implicit val `Write[Long]`: Write[Long] = Write( _.writeLong( _ ) )

    implicit val `Write[PersistableBundle]`: Write[PersistableBundle] = new Write[PersistableBundle] {
        @TargetApi( 21 )
        override def write( destination: Parcel, value: PersistableBundle, flags: Int ) = {
            destination.writePersistableBundle( value )
        }
    }

    implicit val `Write[Short]`: Write[Short] = Write( _.writeValue( _ ) )

    implicit val `Write[Size]`: Write[Size] = new Write[Size] {
        @TargetApi( 21 )
        override def write( destination: Parcel, value: Size, flags: Int ) = destination.writeSize( value )
    }

    implicit val `Write[SizeF]`: Write[SizeF] = new Write[SizeF] {
        @TargetApi( 21 )
        override def write( destination: Parcel, value: SizeF, flags: Int ) = destination.writeSizeF( value )
    }

    implicit val `Write[SparceBooleanArray]`: Write[SparseBooleanArray] = Write( _.writeSparseBooleanArray( _ ) )

    implicit val `Write[URL]`: Write[URL] = Write[URL] { ( destination, value ) ⇒
        destination.writeString( value.toExternalForm )
    }

    implicit val `Write[Traversable[Binder]]`: Write[Traversable[IBinder]] = new Write[Traversable[IBinder]] {
        override def write( destination: Parcel, value: Traversable[IBinder], flags: Int ) = {
            `Write[Array[Binder]`.write( destination, value.toArray, flags )
        }
    }

    implicit val `Write[Traversable[Boolean]]`: Write[Traversable[Boolean]] = new Write[Traversable[Boolean]] {
        override def write( destination: Parcel, value: Traversable[Boolean], flags: Int ) = {
            `Write[Array[Boolean]`.write( destination, value.toArray, flags )
        }
    }

    implicit val `Write[Traversable[Byte]]`: Write[Traversable[Byte]] = new Write[Traversable[Byte]] {
        override def write( destination: Parcel, value: Traversable[Byte], flags: Int ) = {
            `Write[Array[Byte]`.write( destination, value.toArray, flags )
        }
    }

    implicit val `Write[Traversable[Char]]`: Write[Traversable[Char]] = new Write[Traversable[Char]] {
        override def write( destination: Parcel, value: Traversable[Char], flags: Int ) = {
            `Write[Array[Char]`.write( destination, value.toArray, flags )
        }
    }

    implicit val `Write[Traversable[Double]]`: Write[Traversable[Double]] = new Write[Traversable[Double]] {
        override def write( destination: Parcel, value: Traversable[Double], flags: Int ) = {
            `Write[Array[Double]`.write( destination, value.toArray, flags )
        }
    }

    implicit val `Write[Traversable[Float]]`: Write[Traversable[Float]] = new Write[Traversable[Float]] {
        override def write( destination: Parcel, value: Traversable[Float], flags: Int ) = {
            `Write[Array[Float]`.write( destination, value.toArray, flags )
        }
    }

    implicit val `Write[Traversable[Int]]`: Write[Traversable[Int]] = new Write[Traversable[Int]] {
        override def write( destination: Parcel, value: Traversable[Int], flags: Int ) = {
            `Write[Array[Int]`.write( destination, value.toArray, flags )
        }
    }

    implicit val `Write[Traversable[Long]]`: Write[Traversable[Long]] = new Write[Traversable[Long]] {
        override def write( destination: Parcel, value: Traversable[Long], flags: Int ) = {
            `Write[Array[Long]`.write( destination, value.toArray, flags )
        }
    }

    implicit def `Write[Traversable[Parcelize]]`[T: Write: ClassTag]: Write[Traversable[T]] = new Write[Traversable[T]] {
        override def write( destination: Parcel, value: Traversable[T], flags: Int ) = {
            `Write[Array[Parcelize]]`[T].write( destination, value.toArray, flags )
        }
    }

    implicit val `Write[Traversable[String]]`: Write[Traversable[String]] = new Write[Traversable[String]] {
        override def write( destination: Parcel, value: Traversable[String], flags: Int ) = {
            `Write[Array[String]`.write( destination, value.toArray, flags )
        }
    }
}

object Write extends Write0 {
    def apply[T]( f: ( Parcel, T ) ⇒ Unit ): Write[T] = new Write[T] {
        def write( destination: Parcel, value: T, flags: Int ) = f( destination, value )
    }
}