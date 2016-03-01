package io.taig.android.soap.bundle

import java.net.URL

import android.annotation.TargetApi
import android.os.Parcelable
import android.util.{ Size, SizeF, SparseArray }
import cats.Functor
import cats.syntax.functor._
import io.taig.android.soap
import io.taig.android.soap._
import io.taig.android.soap.syntax.bundle._
import io.taig.android.soap.util.printBundle
import julienrf.enum.Enum
import shapeless.Lazy

import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import scala.reflect.ClassTag

trait Decoder[V] extends soap.Decoder.Guarded[Bundle, V] {
    override protected def printHost( bundle: Bundle ) = printBundle( bundle )

    override protected def contains( bundle: Bundle, key: String ) = bundle.containsKey( key )
}

object Decoder extends DecoderOperations with Decoders0

trait Decoders0 extends DecoderOperations with Decoders1 {
    implicit val decoderArrayBoolean: Decoder[Array[Boolean]] = Decoder.instance( _.getBooleanArray( _ ) )

    implicit val decoderArrayByte: Decoder[Array[Byte]] = Decoder.instance( _.getByteArray( _ ) )

    implicit val decoderArrayChar: Decoder[Array[Char]] = Decoder.instance( _.getCharArray( _ ) )

    implicit val decoderArrayDouble: Decoder[Array[Double]] = Decoder.instance( _.getDoubleArray( _ ) )

    implicit val decoderArrayFloat: Decoder[Array[Float]] = Decoder.instance( _.getFloatArray( _ ) )

    implicit val decoderArrayInt: Decoder[Array[Int]] = Decoder.instance( _.getIntArray( _ ) )

    implicit val decoderArrayLong: Decoder[Array[Long]] = Decoder.instance( _.getLongArray( _ ) )

    implicit def decoderArrayParcelable[V <: Parcelable: ClassTag]: Decoder[Array[V]] = {
        Decoder[Iterable[V]].map( _.toArray )
    }

    implicit val decoderArrayShort: Decoder[Array[Short]] = Decoder.instance( _.getShortArray( _ ) )

    implicit val decoderArrayString: Decoder[Array[String]] = Decoder.instance( _.getStringArray( _ ) )

    implicit val decoderBoolean: Decoder[Boolean] = Decoder.instance( _.getBoolean( _ ) )

    implicit val decoderBundle: Decoder[Bundle] = Decoder.instance( _.getBundle( _ ) )

    implicit val decoderByte: Decoder[Byte] = Decoder.instance( _.getByte( _ ) )

    implicit val decoderChar: Decoder[Char] = Decoder.instance( _.getChar( _ ) )

    implicit val decoderCharSequence: Decoder[CharSequence] = Decoder.instance( _.getCharSequence( _ ) )

    implicit val decoderDouble: Decoder[Double] = Decoder.instance( _.getDouble( _ ) )

    implicit def decoderEnumeration[V: Enum.Derived]: Decoder[V] = Decoder[String].map( Enum[V].decodeOpt( _ ).get )

    implicit val decoderFloat: Decoder[Float] = Decoder.instance( _.getFloat( _ ) )

    implicit val decoderInt: Decoder[Int] = Decoder.instance( _.getInt( _ ) )

    implicit def decoderIterableParcelable[V <: Parcelable, I[V] <: Iterable[V]](
        implicit
        cbf: CanBuildFrom[Nothing, V, I[V]]
    ): Decoder[I[V]] = Decoder.instance {
        case ( bundle, key ) ⇒
            import collection.JavaConversions._
            bundle.getParcelableArrayList[V]( key ).to[I]
    }

    implicit val decoderLong: Decoder[Long] = Decoder.instance( _.getLong( _ ) )

    implicit def decoderOption[V]( implicit d: Lazy[Decoder[V]] ): Decoder[Option[V]] = new Decoder[Option[V]] {
        override def decode( serialization: ( Bundle, String ) ) = decodeRaw( serialization )

        override def decodeRaw( serialization: ( Bundle, String ) ) = serialization match {
            case ( bundle, key ) ⇒ bundle.containsKey( key ) match {
                case true  ⇒ Option( bundle.read[V]( key )( d.value ) )
                case false ⇒ None
            }
        }
    }

    implicit def decoderParcelable[V <: Parcelable]: Decoder[V] = Decoder.instance( _.getParcelable[V]( _ ) )

    implicit val decoderShort: Decoder[Short] = Decoder.instance( _.getShort( _ ) )

    implicit val decoderSize: Decoder[Size] = new Decoder[Size] {
        @TargetApi( 21 )
        override def decodeRaw( serialization: ( Bundle, String ) ) = serialization match {
            case ( bundle, key ) ⇒ bundle.getSize( key )
        }
    }

    implicit val decoderSizeF: Decoder[SizeF] = new Decoder[SizeF] {
        @TargetApi( 21 )
        override def decodeRaw( serialization: ( Bundle, String ) ) = serialization match {
            case ( bundle, key ) ⇒ bundle.getSizeF( key )
        }
    }

    implicit def decoderSparseArrayParcelable[V <: Parcelable]: Decoder[SparseArray[V]] = {
        Decoder.instance( _.getSparseParcelableArray[V]( _ ) )
    }

    implicit val decoderString: Decoder[String] = Decoder.instance( _.getString( _ ) )

    implicit def decoderTraversable[V, T[V] <: Traversable[V]](
        implicit
        d:   Lazy[Decoder[Array[V]]],
        cbf: CanBuildFrom[Array[V], V, T[V]]
    ): Decoder[T[V]] = d.map( _.map[T[V]]( _.to[T] ) ).value

    implicit val decoderURL: Decoder[URL] = Decoder[String].map( new URL( _ ) )
}

trait Decoders1 extends DecoderOperations {
    implicit def decoderBundlerEncoder[V]( implicit d: Lazy[bundler.Decoder[V]] ): Decoder[V] = {
        Decoder.instance{ case ( bundle, key ) ⇒ d.value.decode( bundle.read[Bundle]( key ) ) }
    }
}

trait DecoderOperations {
    def apply[V: Decoder]: Decoder[V] = implicitly[Decoder[V]]

    def instance[V]( f: ( Bundle, String ) ⇒ V ): Decoder[V] = new Decoder[V] {
        override def decodeRaw( serialization: ( Bundle, String ) ) = f.tupled( serialization )
    }

    implicit val functorDecoder: Functor[Decoder] = new Functor[Decoder] {
        override def map[A, B]( b: Decoder[A] )( f: A ⇒ B ) = instance {
            case serialization ⇒ f( b.decode( serialization ) )
        }
    }
}