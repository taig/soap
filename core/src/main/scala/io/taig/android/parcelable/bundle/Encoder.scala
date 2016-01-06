package io.taig.android.parcelable.bundle

import java.net.URL

import android.annotation.TargetApi
import android.os.Parcelable
import android.util.{ SparseArray, SizeF, Size }
import io.taig.android.parcelable
import io.taig.android.parcelable._
import io.taig.android.parcelable.functional._
import io.taig.android.parcelable.syntax._
import julienrf.enum.Enum
import shapeless.Lazy

import scala.language.higherKinds
import scala.reflect.ClassTag

trait Encoder[V] extends parcelable.Encoder[( Bundle, String, V ), Unit]

object Encoder extends EncoderOperations with Encoders0

trait Encoders0 extends EncoderOperations with Encoders1 {
    implicit val `Encoder[Array[Boolean]]`: Encoder[Array[Boolean]] = Encoder.instance( _.putBooleanArray( _, _ ) )

    implicit val `Encoder[Array[Byte]]`: Encoder[Array[Byte]] = Encoder.instance( _.putByteArray( _, _ ) )

    implicit val `Encoder[Array[Char]]`: Encoder[Array[Char]] = Encoder.instance( _.putCharArray( _, _ ) )

    implicit val `Encoder[Array[Double]]`: Encoder[Array[Double]] = Encoder.instance( _.putDoubleArray( _, _ ) )

    implicit val `Encoder[Array[Float]]`: Encoder[Array[Float]] = Encoder.instance( _.putFloatArray( _, _ ) )

    implicit val `Encoder[Array[Int]]`: Encoder[Array[Int]] = Encoder.instance( _.putIntArray( _, _ ) )

    implicit val `Encoder[Array[Long]]`: Encoder[Array[Long]] = Encoder.instance( _.putLongArray( _, _ ) )

    implicit def `Encoder[Array[Parcelable]]`[V <: Parcelable]: Encoder[Array[V]] = {
        Encoder[Iterable[V]].contramap( _.toIterable )
    }

    implicit val `Encoder[Array[Short]]`: Encoder[Array[Short]] = Encoder.instance( _.putShortArray( _, _ ) )

    implicit val `Encoder[Array[String]]`: Encoder[Array[String]] = Encoder.instance( _.putStringArray( _, _ ) )

    implicit val `Encoder[Boolean]`: Encoder[Boolean] = Encoder.instance( _.putBoolean( _, _ ) )

    implicit val `Encoder[Bundle]`: Encoder[Bundle] = Encoder.instance( _.putBundle( _, _ ) )

    implicit val `Encoder[Byte]`: Encoder[Byte] = Encoder.instance( _.putByte( _, _ ) )

    implicit val `Encoder[Char]`: Encoder[Char] = Encoder.instance( _.putChar( _, _ ) )

    implicit val `Encoder[CharSequence]`: Encoder[CharSequence] = Encoder.instance( _.putCharSequence( _, _ ) )

    implicit val `Encoder[Double]`: Encoder[Double] = Encoder.instance( _.putDouble( _, _ ) )

    implicit def `Encoder[Enumeration]`[V: Enum.Derived]: Encoder[V] = Encoder[String].contramap( Enum[V].encode )

    implicit val `Encoder[Float]`: Encoder[Float] = Encoder.instance( _.putFloat( _, _ ) )

    implicit val `Encoder[Int]`: Encoder[Int] = Encoder.instance( _.putInt( _, _ ) )

    implicit def `Encoder[Iterable[Parcelable]]`[V <: Parcelable, I[V] <: Iterable[V]]: Encoder[I[V]] = {
        Encoder.instance {
            case ( bundle, key, value ) ⇒
                import collection.JavaConversions._
                bundle.putParcelableArrayList( key, new java.util.ArrayList[V]( value ) )
        }
    }

    implicit val `Encoder[Long]`: Encoder[Long] = Encoder.instance( _.putLong( _, _ ) )

    implicit def `Encoder[Option]`[V]( implicit e: Lazy[Encoder[V]] ): Encoder[Option[V]] = Encoder.instance {
        case ( bundle, key, value ) ⇒ value.foreach( bundle.write( key, _ )( e.value ) )
    }

    implicit def `Encoder[Parcelable]`[V <: Parcelable]: Encoder[V] = Encoder.instance( _.putParcelable( _, _ ) )

    implicit val `Encoder[Short]`: Encoder[Short] = Encoder.instance( _.putShort( _, _ ) )

    implicit val `Encoder[Size]`: Encoder[Size] = new Encoder[Size] {
        @TargetApi( 21 )
        override def encode( value: ( Bundle, String, Size ) ) = value match {
            case ( bundle, key, value ) ⇒ bundle.putSize( key, value )
        }
    }

    implicit val `Encoder[SizeF]`: Encoder[SizeF] = new Encoder[SizeF] {
        @TargetApi( 21 )
        override def encode( value: ( Bundle, String, SizeF ) ) = value match {
            case ( bundle, key, value ) ⇒ bundle.putSizeF( key, value )
        }
    }

    implicit def `Encoder[SparseArray[Parcelable]]`[V <: Parcelable]: Encoder[SparseArray[V]] = {
        Encoder.instance( _.putSparseParcelableArray( _, _ ) )
    }

    implicit val `Encoder[String]`: Encoder[String] = Encoder.instance( _.putString( _, _ ) )

    implicit def `Encoder[Traversable]`[V: ClassTag, T[V] <: Traversable[V]](
        implicit
        e: Lazy[Encoder[Array[V]]]
    ): Encoder[T[V]] = e.map( _.contramap[T[V]]( _.toArray ) ).value

    implicit val `Encoder[URL]`: Encoder[URL] = Encoder[String].contramap( _.toString )
}

trait Encoders1 extends EncoderOperations {
    implicit def `Encoder[bundler.Encoder]`[V]( implicit e: Lazy[bundler.Encoder[V]] ): Encoder[V] = {
        Encoder.instance{ case ( bundle, key, value ) ⇒ bundle.write[Bundle]( key, e.value.encode( value ) ) }
    }
}

trait EncoderOperations {
    def apply[V: Encoder]: Encoder[V] = implicitly[Encoder[V]]

    def instance[V]( f: ( Bundle, String, V ) ⇒ Unit ): Encoder[V] = new Encoder[V] {
        override def encode( value: ( Bundle, String, V ) ) = f.tupled( value )
    }

    implicit val `Contravariant[Encoder]`: Contravariant[Encoder] = new Contravariant[Encoder] {
        override def contramap[A, B]( b: Encoder[A] )( f: B ⇒ A ) = instance {
            case ( bundle, key, value ) ⇒ b.encode( bundle, key, f( value ) )
        }
    }
}