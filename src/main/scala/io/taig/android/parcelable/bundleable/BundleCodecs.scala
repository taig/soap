package io.taig.android.parcelable.bundleable

import android.annotation.TargetApi
import android.os.{ IBinder, Parcelable }
import android.util.{ Size, SizeF, SparseArray }
import shapeless.Lazy

import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import scala.reflect.ClassTag

private[parcelable] trait BundleCodecs extends LowPriorityBundleCodecs {
    implicit val `Codec.Bundle[Boolean]` = Codec.Bundle[Boolean](
        _.putBoolean( _, _ ),
        _.getBoolean( _ )
    )

    implicit val `Codec.Bundle[Bundle]` = Codec.Bundle[Bundle](
        _.putBundle( _, _ ),
        _.getBundle( _ )
    )

    implicit val `Codec.Bundle[Byte]` = Codec.Bundle[Byte](
        _.putByte( _, _ ),
        _.getByte( _ )
    )

    implicit val `Codec.Bundle[Char]` = Codec.Bundle[Char](
        _.putChar( _, _ ),
        _.getChar( _ )
    )

    implicit val `Codec.Bundle[CharSequence]` = Codec.Bundle[CharSequence](
        _.putCharSequence( _, _ ),
        _.getCharSequence( _ )
    )

    implicit val `Codec.Bundle[Double]` = Codec.Bundle[Double](
        _.putDouble( _, _ ),
        _.getDouble( _ )
    )

    implicit val `Codec.Bundle[IBinder]` = new Codec.Bundle[IBinder] {
        @TargetApi( 18 )
        override def encode( bundle: Bundle, key: String, value: IBinder ) = bundle.putBinder( key, value )

        @TargetApi( 18 )
        override def decode( bundle: Bundle, key: String ) = bundle.getBinder( key )
    }

    implicit val `Codec.Bundle[Float]` = Codec.Bundle[Float](
        _.putFloat( _, _ ),
        _.getFloat( _ )
    )

    implicit val `Codec.Bundle[Int]` = Codec.Bundle[Int](
        _.putInt( _, _ ),
        _.getInt( _ )
    )

    implicit val `Codec.Bundle[Long]` = Codec.Bundle[Long](
        _.putLong( _, _ ),
        _.getLong( _ )
    )

    implicit val `Codec.Bundle[Short]` = Codec.Bundle[Short](
        _.putShort( _, _ ),
        _.getShort( _ )
    )

    implicit val `Codec.Bundle[Size]` = new Codec.Bundle[Size] {
        @TargetApi( 21 )
        override def encode( bundle: Bundle, key: String, value: Size ) = bundle.putSize( key, value )

        @TargetApi( 21 )
        override def decode( bundle: Bundle, key: String ) = bundle.getSize( key )
    }

    implicit val `Codec.Bundle[SizeF]` = new Codec.Bundle[SizeF] {
        @TargetApi( 21 )
        override def encode( bundle: Bundle, key: String, value: SizeF ) = bundle.putSizeF( key, value )

        @TargetApi( 21 )
        override def decode( bundle: Bundle, key: String ) = bundle.getSizeF( key )
    }

    implicit def `Codec.Bundle[SparseArray[Parcelable]]`[T <: Parcelable] = Codec.Bundle[SparseArray[T]](
        _.putSparseParcelableArray( _, _ ),
        _.getSparseParcelableArray[T]( _ )
    )

    implicit val `Codec.Bundle[String]` = Codec.Bundle[String](
        _.putString( _, _ ),
        _.getString( _ )
    )

    implicit def `Codec.Bundle[Traversable]`[A: ClassTag, T[X] <: Traversable[X]](
        implicit
        c:   Codec.Bundle[Array[A]],
        cbf: CanBuildFrom[Nothing, A, T[A]]
    ) = {
        Codec.Bundle[T[A]](
            ( bundle, key, value ) ⇒ c.encode( bundle, key, value.toArray ),
            ( bundle, key ) ⇒ c.decode( bundle, key ).to[T]
        )
    }
}

private[parcelable] trait LowPriorityBundleCodecs {
    implicit def `Codec.Bundle[Bundler]`[T]( implicit b: Lazy[Bundler[T]] ): Codec.Bundle[T] = ???
}