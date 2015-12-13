package io.taig.android.parcelable.bundle

import java.net.URL

import android.annotation.TargetApi
import android.os.Parcelable
import android.util.{ Size, SizeF, SparseArray }
import cats.functor.Contravariant
import cats.syntax.contravariant._
import export.imports
import io.taig.android.parcelable
import io.taig.android.parcelable._

import scala.language.higherKinds
import scala.reflect.ClassTag

trait Encoder[V] extends parcelable.Encoder {
    override type Value = ( Bundle, String, V )

    override type Serialization = Unit
}

object Encoder extends EncoderOperations with Encoders0

@imports[Encoder]
trait Encoders0 extends EncoderOperations with Encoders1 {
    implicit val `Encoder[Array[Byte]]`: Encoder[Array[Byte]] = Encoder( _.putByteArray( _, _ ) )

    implicit val `Encoder[Array[Boolean]]`: Encoder[Array[Boolean]] = Encoder( _.putBooleanArray( _, _ ) )

    implicit val `Encoder[Array[Char]]`: Encoder[Array[Char]] = Encoder( _.putCharArray( _, _ ) )

    implicit val `Encoder[Array[Double]]`: Encoder[Array[Double]] = Encoder( _.putDoubleArray( _, _ ) )

    implicit val `Encoder[Array[Float]]`: Encoder[Array[Float]] = Encoder( _.putFloatArray( _, _ ) )

    implicit val `Encoder[Array[Int]]`: Encoder[Array[Int]] = Encoder( _.putIntArray( _, _ ) )

    implicit val `Encoder[Array[Long]]`: Encoder[Array[Long]] = Encoder( _.putLongArray( _, _ ) )

    implicit def `Encoder[Array[Parcelable]]`[V <: Parcelable]: Encoder[Array[V]] = {
        `Encoder[Iterable[Parcelable]]`[V, Iterable].contramap( _.toIterable )
    }

    implicit val `Encoder[Array[Short]]`: Encoder[Array[Short]] = Encoder( _.putShortArray( _, _ ) )

    implicit val `Encoder[Array[String]]`: Encoder[Array[String]] = Encoder( _.putStringArray( _, _ ) )

    implicit val `Encoder[Boolean]`: Encoder[Boolean] = Encoder( _.putBoolean( _, _ ) )

    implicit val `Encoder[Bundle]`: Encoder[Bundle] = Encoder( _.putBundle( _, _ ) )

    implicit val `Encoder[Byte]`: Encoder[Byte] = Encoder( _.putByte( _, _ ) )

    implicit val `Encoder[Char]`: Encoder[Char] = Encoder( _.putChar( _, _ ) )

    implicit val `Encoder[CharSequence]`: Encoder[CharSequence] = Encoder( _.putCharSequence( _, _ ) )

    implicit val `Encoder[Double]`: Encoder[Double] = Encoder( _.putDouble( _, _ ) )

    implicit val `Encoder[Float]`: Encoder[Float] = Encoder( _.putFloat( _, _ ) )

    implicit val `Encoder[Int]`: Encoder[Int] = Encoder( _.putInt( _, _ ) )

    implicit def `Encoder[Iterable[Parcelable]]`[V <: Parcelable, I[V] <: Iterable[V]]: Encoder[I[V]] = {
        Encoder { ( bundle, key, value ) ⇒
            import collection.JavaConversions._
            bundle.putParcelableArrayList( key, new java.util.ArrayList[V]( value ) )
        }
    }

    implicit val `Encoder[Long]`: Encoder[Long] = Encoder( _.putLong( _, _ ) )

    implicit def `Encoder[Option]`[V: Encoder]: Encoder[Option[V]] = Encoder {
        case ( bundle, key, value ) ⇒ value.foreach( bundle.write( key, _ ) )
    }

    implicit def `Encoder[Parcelable]`[V <: Parcelable]: Encoder[V] = Encoder( _.putParcelable( _, _ ) )

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
        Encoder( _.putSparseParcelableArray( _, _ ) )
    }

    implicit val `Encoder[Short]`: Encoder[Short] = Encoder( _.putShort( _, _ ) )

    implicit val `Encoder[String]`: Encoder[String] = Encoder( _.putString( _, _ ) )

    implicit def `Encoder[Traversable]`[V: ClassTag, T[V] <: Traversable[V]](
        implicit
        e: Encoder[Array[V]]
    ): Encoder[T[V]] = e.contramap( _.toArray )

    implicit val `Encoder[URL]`: Encoder[URL] = `Encoder[String]`.contramap( _.toString )
}

trait Encoders1 extends EncoderOperations {
    implicit def `Encoder[bundler.Encoder]`[V: bundler.Encoder]: Encoder[V] = Encoder {
        case ( bundle, key, value ) ⇒ bundle.write( key, implicitly[bundler.Encoder[V]].encode( value ) )
    }
}

trait EncoderOperations {
    def apply[V]( f: ( Bundle, String, V ) ⇒ Unit ): Encoder[V] = new Encoder[V] {
        override def encode( value: ( Bundle, String, V ) ) = f.tupled( value )
    }

    implicit val `Contravariant[Bundle]`: Contravariant[Encoder] = new Contravariant[Encoder] {
        override def contramap[A, B]( b: Encoder[A] )( f: B ⇒ A ) = new Encoder[B] {
            override def encode( value: ( Bundle, String, B ) ) = b.encode( value.copy( _3 = f( value._3 ) ) )
        }
    }
}