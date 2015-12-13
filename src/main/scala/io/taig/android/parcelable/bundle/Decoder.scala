package io.taig.android.parcelable.bundle

import java.net.URL

import android.annotation.TargetApi
import android.os.Parcelable
import android.util.{ Size, SizeF, SparseArray }
import cats.Functor
import cats.syntax.functor._
import export.imports
import io.taig.android.parcelable
import io.taig.android.parcelable._
import shapeless.Lazy

import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import scala.reflect.ClassTag

trait Decoder[V] extends parcelable.Decoder {
    override type Serialization = ( Bundle, String )

    override type Value = V

    override def decode( serialization: Serialization ): V = serialization match {
        case ( bundle, key ) ⇒
            if ( bundle.containsKey( key ) ) {
                decodeRaw( serialization )
            } else {
                throw exception.KeyNotFound( key )
            }
    }

    def decodeRaw( serialization: Serialization ): V
}

object Decoder extends DecoderOperations with Decoders0

@imports[Decoder]
trait Decoders0 extends DecoderOperations with Decoders1 {
    implicit val `Decoder[Array[Boolean]]`: Decoder[Array[Boolean]] = Decoder( _.getBooleanArray( _ ) )

    implicit val `Decoder[Array[Byte]]`: Decoder[Array[Byte]] = Decoder( _.getByteArray( _ ) )

    implicit val `Decoder[Array[Char]]`: Decoder[Array[Char]] = Decoder( _.getCharArray( _ ) )

    implicit val `Decoder[Array[Double]]`: Decoder[Array[Double]] = Decoder( _.getDoubleArray( _ ) )

    implicit val `Decoder[Array[Float]]`: Decoder[Array[Float]] = Decoder( _.getFloatArray( _ ) )

    implicit val `Decoder[Array[Int]]`: Decoder[Array[Int]] = Decoder( _.getIntArray( _ ) )

    implicit val `Decoder[Array[Long]]`: Decoder[Array[Long]] = Decoder( _.getLongArray( _ ) )

    implicit def `Decoder[Array[Parcelable]]`[V <: Parcelable: ClassTag]: Decoder[Array[V]] = {
        `Decoder[Iterable[Parcelable]]`[V, Iterable].map( _.toArray )
    }

    implicit val `Decoder[Array[Short]]`: Decoder[Array[Short]] = Decoder( _.getShortArray( _ ) )

    implicit val `Decoder[Array[String]]`: Decoder[Array[String]] = Decoder( _.getStringArray( _ ) )

    implicit val `Decoder[Boolean]`: Decoder[Boolean] = Decoder( _.getBoolean( _ ) )

    implicit val `Decoder[Bundle]`: Decoder[Bundle] = Decoder( _.getBundle( _ ) )

    implicit val `Decoder[Byte]`: Decoder[Byte] = Decoder( _.getByte( _ ) )

    implicit val `Decoder[Char]`: Decoder[Char] = Decoder( _.getChar( _ ) )

    implicit val `Decoder[CharSequence]`: Decoder[CharSequence] = Decoder( _.getCharSequence( _ ) )

    implicit val `Decoder[Double]`: Decoder[Double] = Decoder( _.getDouble( _ ) )

    implicit val `Decoder[Float]`: Decoder[Float] = Decoder( _.getFloat( _ ) )

    implicit val `Decoder[Int]`: Decoder[Int] = Decoder( _.getInt( _ ) )

    implicit def `Decoder[Iterable[Parcelable]]`[V <: Parcelable, I[V] <: Iterable[V]](
        implicit
        cbf: CanBuildFrom[Nothing, V, I[V]]
    ): Decoder[I[V]] = {
        Decoder { ( bundle, key ) ⇒
            import collection.JavaConversions._
            bundle.getParcelableArrayList[V]( key ).to[I]
        }
    }

    implicit val `Decoder[Long]`: Decoder[Long] = Decoder( _.getLong( _ ) )

    implicit def `Decoder[Option]`[V]( implicit d: Lazy[Decoder[V]] ): Decoder[Option[V]] = new Decoder[Option[V]] {
        override def decode( serialization: ( Bundle, String ) ) = decodeRaw( serialization )

        override def decodeRaw( serialization: ( Bundle, String ) ) = serialization match {
            case ( bundle, key ) ⇒ bundle.containsKey( key ) match {
                case true  ⇒ Option( bundle.read[V]( key )( d.value ) )
                case false ⇒ None
            }
        }
    }

    implicit def `Decoder[Parcelable]`[V <: Parcelable]: Decoder[V] = Decoder( _.getParcelable[V]( _ ) )

    implicit val `Decoder[Size]`: Decoder[Size] = new Decoder[Size] {
        @TargetApi( 21 )
        override def decodeRaw( serialization: ( Bundle, String ) ) = serialization match {
            case ( bundle, key ) ⇒ bundle.getSize( key )
        }
    }

    implicit val `Decoder[SizeF]`: Decoder[SizeF] = new Decoder[SizeF] {
        @TargetApi( 21 )
        override def decodeRaw( serialization: ( Bundle, String ) ) = serialization match {
            case ( bundle, key ) ⇒ bundle.getSizeF( key )
        }
    }

    implicit def `Decoder[SparseArray[Parcelable]]`[V <: Parcelable]: Decoder[SparseArray[V]] = {
        Decoder( _.getSparseParcelableArray( _ ) )
    }

    implicit val `Decoder[Short]`: Decoder[Short] = Decoder( _.getShort( _ ) )

    implicit val `Decoder[String]`: Decoder[String] = Decoder( _.getString( _ ) )

    implicit def `Decoder[Traversable]`[V, T[V] <: Traversable[V]](
        implicit
        d:   Lazy[Decoder[Array[V]]],
        cbf: CanBuildFrom[Array[V], V, T[V]]
    ): Decoder[T[V]] = d.map( _.map( _.to[T] ) ).value

    implicit val `Decoder[URL]`: Decoder[URL] = `Decoder[String]`.map( new URL( _ ) )
}

trait Decoders1 extends DecoderOperations {
    implicit def `Decoder[bundler.Decoder]`[V]( implicit d: Lazy[bundler.Decoder[V]] ): Decoder[V] = Decoder {
        case ( bundle, key ) ⇒ d.value.decode( bundle.read[Bundle]( key ) )
    }
}

trait DecoderOperations {
    def apply[V]( f: ( Bundle, String ) ⇒ V ): Decoder[V] = new Decoder[V] {
        override def decodeRaw( serialization: ( Bundle, String ) ) = f.tupled( serialization )
    }

    implicit val `Functor[Bundle]`: Functor[Decoder] = new Functor[Decoder] {
        override def map[A, B]( b: Decoder[A] )( f: A ⇒ B ) = new Decoder[B] {
            override def decodeRaw( serialization: ( Bundle, String ) ) = f( b.decode( serialization ) )
        }
    }
}