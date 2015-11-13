package io.taig.android.parcelable.bundleize

import android.annotation.TargetApi
import android.os.{ IBinder, Parcelable ⇒ AParcelable }
import android.util.{ Size, SizeF }
import io.taig.android.parcelable._
import io.taig.android.parcelable.internal._
import shapeless.Lazy

import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds

/**
 * Type class that instructs how to read a value from a given Bundle
 */
trait Read[T] {
    def read( bundle: Bundle, key: String ): T
}

trait Read1 {
    implicit def `Read[Bundleable]`[T]( implicit r: Lazy[bundleable.Read[T]] ): Read[T] = Read { ( bundle, key ) ⇒
        r.value.read( bundle.getBundle( key ) )
    }
}

trait Read0 extends Read1 {
    implicit val `Read[Array[Boolean]`: Read[Array[Boolean]] = Read( _.getBooleanArray( _ ) )

    implicit val `Read[Array[Byte]`: Read[Array[Byte]] = Read( _.getByteArray( _ ) )

    implicit val `Read[Array[Char]`: Read[Array[Char]] = Read( _.getCharArray( _ ) )

    implicit val `Read[Array[Double]`: Read[Array[Double]] = Read( _.getDoubleArray( _ ) )

    implicit val `Read[Array[Float]`: Read[Array[Float]] = Read( _.getFloatArray( _ ) )

    implicit val `Read[Array[Int]`: Read[Array[Int]] = Read( _.getIntArray( _ ) )

    implicit val `Read[Array[Long]`: Read[Array[Long]] = Read( _.getLongArray( _ ) )

    implicit val `Read[Array[Parcelable]`: Read[Array[AParcelable]] = Read( _.getParcelableArray( _ ) )

    implicit val `Read[Array[Short]`: Read[Array[Short]] = Read( _.getShortArray( _ ) )

    implicit val `Read[Array[String]`: Read[Array[String]] = Read( _.getStringArray( _ ) )

    implicit val `Read[Boolean]`: Read[Boolean] = Read( _.getBoolean( _ ) )

    implicit val `Read[Bundle]`: Read[Bundle] = Read( _.getBundle( _ ) )

    implicit val `Read[Byte]`: Read[Byte] = Read( _.getByte( _ ) )

    implicit val `Read[Char]`: Read[Char] = Read( _.getChar( _ ) )

    implicit val `Read[CharSequence]`: Read[CharSequence] = Read( _.getCharSequence( _ ) )

    implicit val `Read[Double]`: Read[Double] = Read( _.getDouble( _ ) )

    implicit val `Read[IBinder]`: Read[IBinder] = new Read[IBinder] {
        @TargetApi( 18 )
        override def read( bundle: Bundle, key: String ) = bundle.checked( key )( _.getBinder( _ ) )
    }

    implicit val `Read[Float]`: Read[Float] = Read( _.getFloat( _ ) )

    implicit val `Read[Int]`: Read[Int] = Read( _.getInt( _ ) )

    implicit val `Read[Long]`: Read[Long] = Read( _.getLong( _ ) )

    implicit def `Read[Parcelable]`[T <: AParcelable]: Read[T] = Read[T]( _.getParcelable[T]( _ ) )

    implicit val `Read[Short]`: Read[Short] = Read( _.getShort( _ ) )

    implicit val `Read[Size]`: Read[Size] = new Read[Size] {
        @TargetApi( 21 )
        override def read( bundle: Bundle, key: String ) = bundle.checked( key )( _.getSize( _ ) )
    }

    implicit val `Read[SizeF]`: Read[SizeF] = new Read[SizeF] {
        @TargetApi( 21 )
        override def read( bundle: Bundle, key: String ) = bundle.checked( key )( _.getSizeF( _ ) )
    }

    implicit val `Read[String]`: Read[String] = Read( _.getString( _ ) )

    implicit def `Read[Traversable[Boolean]]`[L[B] <: Traversable[B]](
        implicit
        cbf: CanBuildFrom[Nothing, Boolean, L[Boolean]]
    ): Read[L[Boolean]] = `Read[Array[Boolean]`.map( _.to[L] )

    implicit def `Read[Traversable[Byte]]`[L[B] <: Traversable[B]](
        implicit
        cbf: CanBuildFrom[Nothing, Byte, L[Byte]]
    ): Read[L[Byte]] = `Read[Array[Byte]`.map( _.to[L] )

    implicit def `Read[Traversable[Char]]`[L[B] <: Traversable[B]](
        implicit
        cbf: CanBuildFrom[Nothing, Char, L[Char]]
    ): Read[L[Char]] = `Read[Array[Char]`.map( _.to[L] )

    implicit def `Read[Traversable[Double]]`[L[B] <: Traversable[B]](
        implicit
        cbf: CanBuildFrom[Nothing, Double, L[Double]]
    ): Read[L[Double]] = `Read[Array[Double]`.map( _.to[L] )

    implicit def `Read[Traversable[Float]]`[L[B] <: Traversable[B]](
        implicit
        cbf: CanBuildFrom[Nothing, Float, L[Float]]
    ): Read[L[Float]] = `Read[Array[Float]`.map( _.to[L] )

    implicit def `Read[Traversable[Int]]`[L[B] <: Traversable[B]](
        implicit
        cbf: CanBuildFrom[Nothing, Int, L[Int]]
    ): Read[L[Int]] = `Read[Array[Int]`.map( _.to[L] )

    implicit def `Read[Traversable[Long]]`[L[B] <: Traversable[B]](
        implicit
        cbf: CanBuildFrom[Nothing, Long, L[Long]]
    ): Read[L[Long]] = `Read[Array[Long]`.map( _.to[L] )

    implicit def `Read[Traversable[Parcelable]]`[L[B] <: Traversable[B]](
        implicit
        cbf: CanBuildFrom[Nothing, android.os.Parcelable, L[android.os.Parcelable]]
    ): Read[L[AParcelable]] = `Read[Array[Parcelable]`.map( _.to[L] )

    implicit def `Read[Traversable[Short]]`[L[B] <: Traversable[B]](
        implicit
        cbf: CanBuildFrom[Nothing, Short, L[Short]]
    ): Read[L[Short]] = `Read[Array[Short]`.map( _.to[L] )

    implicit def `Read[Traversable[String]]`[L[B] <: Traversable[B]](
        implicit
        cbf: CanBuildFrom[Nothing, String, L[String]]
    ): Read[L[String]] = `Read[Array[String]`.map( _.to[L] )
}

object Read extends Read0 {
    def apply[T]( f: ( Bundle, String ) ⇒ T ) = new Read[T] {
        override def read( bundle: Bundle, key: String ) = bundle.checked( key )( f )
    }
}