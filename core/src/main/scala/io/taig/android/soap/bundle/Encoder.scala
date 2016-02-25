package io.taig.android.soap.bundle

import java.net.URL

import android.annotation.TargetApi
import android.os.Parcelable
import android.util.{ SparseArray, SizeF, Size }
import io.taig.android.soap
import io.taig.android.soap._
import io.taig.android.soap.functional._
import io.taig.android.soap.syntax.bundle._
import io.taig.android.soap.syntax.functional._
import julienrf.enum.Enum
import shapeless.Lazy

import scala.language.higherKinds
import scala.reflect.ClassTag

trait Encoder[V] extends soap.Encoder[( Bundle, String, V ), Unit]

object Encoder extends EncoderOperations with Encoders0

trait Encoders0 extends EncoderOperations with Encoders1 {
    implicit val encoderArrayBoolean: Encoder[Array[Boolean]] = Encoder.instance( _.putBooleanArray( _, _ ) )

    implicit val encoderArrayByte: Encoder[Array[Byte]] = Encoder.instance( _.putByteArray( _, _ ) )

    implicit val encoderArrayChar: Encoder[Array[Char]] = Encoder.instance( _.putCharArray( _, _ ) )

    implicit val encoderArrayDouble: Encoder[Array[Double]] = Encoder.instance( _.putDoubleArray( _, _ ) )

    implicit val encoderArrayFloat: Encoder[Array[Float]] = Encoder.instance( _.putFloatArray( _, _ ) )

    implicit val encoderArrayInt: Encoder[Array[Int]] = Encoder.instance( _.putIntArray( _, _ ) )

    implicit val encoderArrayLong: Encoder[Array[Long]] = Encoder.instance( _.putLongArray( _, _ ) )

    implicit def encoderArrayParcelable[V <: Parcelable]: Encoder[Array[V]] = {
        Encoder[Iterable[V]].contramap( _.toIterable )
    }

    implicit val encoderArrayShort: Encoder[Array[Short]] = Encoder.instance( _.putShortArray( _, _ ) )

    implicit val encoderArrayString: Encoder[Array[String]] = Encoder.instance( _.putStringArray( _, _ ) )

    implicit val encoderBoolean: Encoder[Boolean] = Encoder.instance( _.putBoolean( _, _ ) )

    implicit val encoderBundle: Encoder[Bundle] = Encoder.instance( _.putBundle( _, _ ) )

    implicit val encoderByte: Encoder[Byte] = Encoder.instance( _.putByte( _, _ ) )

    implicit val encoderChar: Encoder[Char] = Encoder.instance( _.putChar( _, _ ) )

    implicit val encoderCharSequence: Encoder[CharSequence] = Encoder.instance( _.putCharSequence( _, _ ) )

    implicit val encoderDouble: Encoder[Double] = Encoder.instance( _.putDouble( _, _ ) )

    implicit def encoderEnumeration[V: Enum.Derived]: Encoder[V] = Encoder[String].contramap( Enum[V].encode )

    implicit val encoderFloat: Encoder[Float] = Encoder.instance( _.putFloat( _, _ ) )

    implicit val encoderInt: Encoder[Int] = Encoder.instance( _.putInt( _, _ ) )

    implicit def encoderIterableParcelable[V <: Parcelable, I[V] <: Iterable[V]]: Encoder[I[V]] = {
        Encoder.instance {
            case ( bundle, key, value ) ⇒
                import collection.JavaConversions._
                bundle.putParcelableArrayList( key, new java.util.ArrayList[V]( value ) )
        }
    }

    implicit val encoderLong: Encoder[Long] = Encoder.instance( _.putLong( _, _ ) )

    implicit def encoderOption[V]( implicit e: Lazy[Encoder[V]] ): Encoder[Option[V]] = Encoder.instance {
        case ( bundle, key, value ) ⇒ value.foreach( bundle.write( key, _ )( e.value ) )
    }

    implicit def encoderParcelable[V <: Parcelable]: Encoder[V] = Encoder.instance( _.putParcelable( _, _ ) )

    implicit val encoderShort: Encoder[Short] = Encoder.instance( _.putShort( _, _ ) )

    implicit val encoderSize: Encoder[Size] = new Encoder[Size] {
        @TargetApi( 21 )
        override def encode( value: ( Bundle, String, Size ) ) = value match {
            case ( bundle, key, value ) ⇒ bundle.putSize( key, value )
        }
    }

    implicit val encoderSizeF: Encoder[SizeF] = new Encoder[SizeF] {
        @TargetApi( 21 )
        override def encode( value: ( Bundle, String, SizeF ) ) = value match {
            case ( bundle, key, value ) ⇒ bundle.putSizeF( key, value )
        }
    }

    implicit def encoderSparseArrayParcelable[V <: Parcelable]: Encoder[SparseArray[V]] = {
        Encoder.instance( _.putSparseParcelableArray( _, _ ) )
    }

    implicit val encoderString: Encoder[String] = Encoder.instance( _.putString( _, _ ) )

    implicit def encoderTraversable[V: ClassTag, T[V] <: Traversable[V]](
        implicit
        e: Lazy[Encoder[Array[V]]]
    ): Encoder[T[V]] = e.map( _.contramap[T[V]]( _.toArray ) ).value

    implicit val encoderURL: Encoder[URL] = Encoder[String].contramap( _.toString )
}

trait Encoders1 extends EncoderOperations {
    implicit def encoderBundlerEncoder[V]( implicit e: Lazy[bundler.Encoder[V]] ): Encoder[V] = {
        Encoder.instance{ case ( bundle, key, value ) ⇒ bundle.write[Bundle]( key, e.value.encode( value ) ) }
    }
}

trait EncoderOperations {
    def apply[V: Encoder]: Encoder[V] = implicitly[Encoder[V]]

    def instance[V]( f: ( Bundle, String, V ) ⇒ Unit ): Encoder[V] = new Encoder[V] {
        override def encode( value: ( Bundle, String, V ) ) = f.tupled( value )
    }

    implicit val contravariantEncoder: Contravariant[Encoder] = new Contravariant[Encoder] {
        override def contramap[A, B]( b: Encoder[A] )( f: B ⇒ A ) = instance {
            case ( bundle, key, value ) ⇒ b.encode( bundle, key, f( value ) )
        }
    }
}