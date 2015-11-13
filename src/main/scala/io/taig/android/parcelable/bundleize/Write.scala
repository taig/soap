package io.taig.android.parcelable.bundleize

import android.annotation.TargetApi
import android.os.IBinder
import android.util.{ SizeF, Size }
import io.taig.android.parcelable._
import shapeless.Lazy
import android.os.{ Parcelable ⇒ AParcelable }

/**
 * Type class that instructs how to read/write a value from/to a given Bundle
 */
trait Write[-T] {
    def write( bundle: Bundle, key: String, value: T ): Unit
}

trait Write1 {
    implicit def `Write[Bundleable]`[T]( implicit w: Lazy[bundleable.Write[T]] ): Write[T] = {
        Write{ ( bundle, key, value ) ⇒ bundle.putBundle( key, w.value.write( value ) ) }
    }
}

trait Write0 extends Write1 {
    implicit val `Write[Array[Boolean]]`: Write[Array[Boolean]] = Write( _.putBooleanArray( _, _ ) )

    implicit val `Write[Array[Byte]]`: Write[Array[Byte]] = Write( _.putByteArray( _, _ ) )

    implicit val `Write[Array[Char]]`: Write[Array[Char]] = Write( _.putCharArray( _, _ ) )

    implicit val `Write[Array[Double]]`: Write[Array[Double]] = Write( _.putDoubleArray( _, _ ) )

    implicit val `Write[Array[Float]]`: Write[Array[Float]] = Write( _.putFloatArray( _, _ ) )

    implicit val `Write[Array[Int]]`: Write[Array[Int]] = Write( _.putIntArray( _, _ ) )

    implicit val `Write[Array[Long]]`: Write[Array[Long]] = Write( _.putLongArray( _, _ ) )

    implicit val `Write[Array[Parcelable]]`: Write[Array[AParcelable]] = Write( _.putParcelableArray( _, _ ) )

    implicit val `Write[Array[Short]]`: Write[Array[Short]] = Write( _.putShortArray( _, _ ) )

    implicit val `Write[Array[String]]`: Write[Array[String]] = Write( _.putStringArray( _, _ ) )

    implicit val `Write[Boolean]`: Write[Boolean] = Write( _.putBoolean( _, _ ) )

    implicit val `Write[Bundle]`: Write[Bundle] = Write( _.putBundle( _, _ ) )

    implicit val `Write[Byte]`: Write[Byte] = Write( _.putByte( _, _ ) )

    implicit val `Write[Char]`: Write[Char] = Write( _.putChar( _, _ ) )

    implicit val `Write[CharSequence]`: Write[CharSequence] = Write( _.putCharSequence( _, _ ) )

    implicit val `Write[Double]`: Write[Double] = Write( _.putDouble( _, _ ) )

    implicit val `Write[IBinder]`: Write[IBinder] = new Write[IBinder] {
        @TargetApi( 18 )
        override def write( bundle: Bundle, key: String, value: IBinder ) = bundle.putBinder( key, value )
    }

    implicit val `Write[Float]`: Write[Float] = Write( _.putFloat( _, _ ) )

    implicit val `Write[Int]`: Write[Int] = Write( _.putInt( _, _ ) )

    implicit val `Write[Long]`: Write[Long] = Write( _.putLong( _, _ ) )

    implicit val `Write[Parcelable]`: Write[android.os.Parcelable] = {
        Write[android.os.Parcelable]( _.putParcelable( _, _ ) )
    }

    implicit val `Write[Short]`: Write[Short] = Write( _.putShort( _, _ ) )

    implicit val `Write[Size]`: Write[Size] = new Write[Size] {
        @TargetApi( 21 )
        override def write( bundle: Bundle, key: String, value: Size ) = bundle.putSize( key, value )
    }

    implicit val `Write[SizeF]`: Write[SizeF] = new Write[SizeF] {
        @TargetApi( 21 )
        override def write( bundle: Bundle, key: String, value: SizeF ) = bundle.putSizeF( key, value )
    }

    implicit val `Write[String]`: Write[String] = Write( _.putString( _, _ ) )

    implicit val `Write[Traversable[Boolean]]`: Write[Traversable[Boolean]] = {
        Write( ( bundle, key, value ) ⇒ `Write[Array[Boolean]]`.write( bundle, key, value.toArray ) )
    }

    implicit val `Write[Traversable[Byte]]`: Write[Traversable[Byte]] = {
        Write( ( bundle, key, value ) ⇒ `Write[Array[Byte]]`.write( bundle, key, value.toArray ) )
    }

    implicit val `Write[Traversable[Char]]`: Write[Traversable[Char]] = {
        Write( ( bundle, key, value ) ⇒ `Write[Array[Char]]`.write( bundle, key, value.toArray ) )
    }

    implicit val `Write[Traversable[Double]]`: Write[Traversable[Double]] = {
        Write( ( bundle, key, value ) ⇒ `Write[Array[Double]]`.write( bundle, key, value.toArray ) )
    }

    implicit val `Write[Traversable[Float]]`: Write[Traversable[Float]] = {
        Write( ( bundle, key, value ) ⇒ `Write[Array[Float]]`.write( bundle, key, value.toArray ) )
    }

    implicit val `Write[Traversable[Int]]`: Write[Traversable[Int]] = {
        Write( ( bundle, key, value ) ⇒ `Write[Array[Int]]`.write( bundle, key, value.toArray ) )
    }

    implicit val `Write[Traversable[Long]]`: Write[Traversable[Long]] = {
        Write( ( bundle, key, value ) ⇒ `Write[Array[Long]]`.write( bundle, key, value.toArray ) )
    }

    implicit val `Write[Traversable[Parcelable]]`: Write[Traversable[AParcelable]] = {
        Write( ( bundle, key, value ) ⇒ `Write[Array[Parcelable]]`.write( bundle, key, value.toArray ) )
    }

    implicit val `Write[Traversable[Short]]`: Write[Traversable[Short]] = {
        Write( ( bundle, key, value ) ⇒ `Write[Array[Short]]`.write( bundle, key, value.toArray ) )
    }

    implicit val `Write[Traversable[String]]`: Write[Traversable[String]] = {
        Write( ( bundle, key, value ) ⇒ `Write[Array[String]]`.write( bundle, key, value.toArray ) )
    }
}

object Write extends Write0 {
    def apply[T]( f: ( Bundle, String, T ) ⇒ Unit ) = new Write[T] {
        override def write( bundle: Bundle, key: String, value: T ) = f( bundle, key, value )
    }
}