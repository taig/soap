package io.taig.android.parcelable.parcelize

import java.io.FileDescriptor
import java.net.URL

import android.annotation.TargetApi
import android.os.{ IBinder, Parcel, PersistableBundle }
import android.text.TextUtils
import android.util.{ Size, SizeF, SparseBooleanArray }
import io.taig.android.parcelable._

import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import scala.reflect.ClassTag

/**
 * Type class that instructs how to read a value from a given Parcel
 */
trait Read[T] {
    def read( source: Parcel ): T

    def map[S]( f: T ⇒ S ): Read[S] = Read{ source ⇒ f( read( source ) ) }
}

trait Read1 {
    implicit def `Write[Bundleable]`[T: bundleable.Read]: Read[T] = Read { source ⇒
        implicitly[bundleable.Read[T]].read( source.readBundle() )
    }
}

trait Read0 extends Read1 {
    implicit val `Read[Array[Binder]]`: Read[Array[IBinder]] = Read( _.createBinderArray )

    implicit val `Read[Array[Boolean]]`: Read[Array[Boolean]] = Read( _.createBooleanArray )

    implicit val `Read[Array[Byte]]`: Read[Array[Byte]] = Read( _.createByteArray )

    implicit val `Read[Array[Char]]`: Read[Array[Char]] = Read( _.createCharArray )

    implicit val `Read[Array[Double]]`: Read[Array[Double]] = Read( _.createDoubleArray )

    implicit val `Read[Array[Float]]`: Read[Array[Float]] = Read( _.createFloatArray )

    implicit val `Read[Array[Int]]`: Read[Array[Int]] = Read( _.createIntArray )

    implicit val `Read[Array[Long]]`: Read[Array[Long]] = Read( _.createLongArray )

    implicit def `Read[Array[Parcelize]]`[T: Read: ClassTag]: Read[Array[T]] = Read { source ⇒
        val array = new Array[T]( source.read[Int] )
        ( 0 to array.length ).foreach( i ⇒ array( i ) = source.read[T] )
        array
    }

    implicit val `Read[Array[String]]`: Read[Array[String]] = Read( _.createStringArray )

    implicit val `Read[Bundle]`: Read[Bundle] = Read( _.readBundle )

    implicit val `Read[Boolean]`: Read[Boolean] = Read( _.readValue( null ).asInstanceOf[Boolean] )

    implicit val `Read[Byte]`: Read[Byte] = Read( _.readByte )

    implicit val `Read[CharSequence]`: Read[CharSequence] = Read[CharSequence] {
        TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel
    }

    implicit val `Read[Double]`: Read[Double] = Read[Double]( _.readDouble )

    implicit val `Read[FileDescriptor]`: Read[FileDescriptor] = Read( _.readFileDescriptor.getFileDescriptor )

    implicit val `Read[Float]`: Read[Float] = Read( _.readFloat )

    implicit val `Read[IBinder]`: Read[IBinder] = Read( _.readStrongBinder )

    implicit val `Read[Int]`: Read[Int] = Read( _.readInt )

    implicit val `Read[Long]`: Read[Long] = Read( _.readLong )

    implicit val `Read[PersistableBundle]`: Read[PersistableBundle] = new Read[PersistableBundle] {
        @TargetApi( 21 )
        override def read( source: Parcel ) = source.readPersistableBundle()
    }

    implicit val `Read[Short]`: Read[Short] = Read( _.readValue( null ).asInstanceOf[Short] )

    implicit val `Read[Size]`: Read[Size] = new Read[Size] {
        @TargetApi( 21 )
        override def read( source: Parcel ) = source.readSize()
    }

    implicit val `Read[SizeF]`: Read[SizeF] = new Read[SizeF] {
        @TargetApi( 21 )
        override def read( source: Parcel ) = source.readSizeF()
    }

    implicit val `Read[SparceBooleanArray]`: Read[SparseBooleanArray] = Read( _.readSparseBooleanArray )

    implicit val `Read[String]`: Read[String] = `Read[CharSequence]`.map {
        case string: String ⇒ string
        case charSequence   ⇒ charSequence.toString
    }

    implicit val `Read[URL]`: Read[URL] = `Read[String]`.map( new URL( _ ) )

    implicit val `Read[Char]`: Read[Char] = `Read[Int]`.map( _.toChar )

    implicit def `Read[Traversable[Binder]]`[L[B] <: Traversable[B]](
        implicit
        cbf: CanBuildFrom[Nothing, IBinder, L[IBinder]]
    ): Read[L[IBinder]] = `Read[Array[Binder]]`.map( _.to[L] )

    implicit def `Read[Traversable[Boolean]]`[L[B] <: Traversable[B]](
        implicit
        cbf: CanBuildFrom[Nothing, Boolean, L[Boolean]]
    ): Read[L[Boolean]] = `Read[Array[Boolean]]`.map( _.to[L] )

    implicit def `Read[Traversable[Byte]]`[L[B] <: Traversable[B]](
        implicit
        cbf: CanBuildFrom[Nothing, Byte, L[Byte]]
    ): Read[L[Byte]] = `Read[Array[Byte]]`.map( _.to[L] )

    implicit def `Read[Traversable[Char]]`[L[B] <: Traversable[B]](
        implicit
        cbf: CanBuildFrom[Nothing, Char, L[Char]]
    ): Read[L[Char]] = `Read[Array[Char]]`.map( _.to[L] )

    implicit def `Read[Traversable[Double]]`[L[B] <: Traversable[B]](
        implicit
        cbf: CanBuildFrom[Nothing, Double, L[Double]]
    ): Read[L[Double]] = `Read[Array[Double]]`.map( _.to[L] )

    implicit def `Read[Traversable[Float]]`[L[B] <: Traversable[B]](
        implicit
        cbf: CanBuildFrom[Nothing, Float, L[Float]]
    ): Read[L[Float]] = `Read[Array[Float]]`.map( _.to[L] )

    implicit def `Read[Traversable[Int]]`[L[B] <: Traversable[B]](
        implicit
        cbf: CanBuildFrom[Nothing, Int, L[Int]]
    ): Read[L[Int]] = `Read[Array[Int]]`.map( _.to[L] )

    implicit def `Read[Traversable[Long]]`[L[B] <: Traversable[B]](
        implicit
        cbf: CanBuildFrom[Nothing, Long, L[Long]]
    ): Read[L[Long]] = `Read[Array[Long]]`.map( _.to[L] )

    implicit def `Read[Traversable[Parcelize]]`[L[B] <: Traversable[B], T: Read: ClassTag](
        implicit
        cbf: CanBuildFrom[Nothing, T, L[T]]
    ): Read[L[T]] = `Read[Array[Parcelize]]`[T].map( _.to[L] )

    implicit def `Read[Traversable[String]]`[L[B] <: Traversable[B]](
        implicit
        cbf: CanBuildFrom[Nothing, String, L[String]]
    ): Read[L[String]] = `Read[Array[String]]`.map( _.to[L] )
}

object Read extends Read0 {
    def apply[T]( f: Parcel ⇒ T ): Read[T] = new Read[T] {
        override def read( source: Parcel ) = f( source )
    }
}