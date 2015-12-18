package io.taig.android.parcelable.bundle

import java.net.URL

import android.annotation.TargetApi
import android.os.Parcelable
import android.util.{ SparseArray, SizeF, Size }
import export.imports
import io.taig.android.parcelable
import io.taig.android.parcelable._
import io.taig.android.parcelable.syntax._
import io.taig.android.parcelable.functional._
import julienrf.enum.Enum
import shapeless.Lazy

import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import scala.reflect.ClassTag

trait Decoder[V] extends parcelable.Decoder.Guarded[Bundle, V] {
    override protected def contains( bundle: Bundle, key: String ) = bundle.containsKey( key )
}

object Decoder extends DecoderOperations with Decoders0

@imports[Decoder]
trait Decoders0 extends DecoderOperations with Decoders1 {
    implicit val `Decoder[Array[Boolean]]`: Decoder[Array[Boolean]] = Decoder.instance( _.getBooleanArray( _ ) )

    implicit val `Decoder[Array[Byte]]`: Decoder[Array[Byte]] = Decoder.instance( _.getByteArray( _ ) )

    implicit val `Decoder[Array[Char]]`: Decoder[Array[Char]] = Decoder.instance( _.getCharArray( _ ) )

    implicit val `Decoder[Array[Double]]`: Decoder[Array[Double]] = Decoder.instance( _.getDoubleArray( _ ) )

    implicit val `Decoder[Array[Float]]`: Decoder[Array[Float]] = Decoder.instance( _.getFloatArray( _ ) )

    implicit val `Decoder[Array[Int]]`: Decoder[Array[Int]] = Decoder.instance( _.getIntArray( _ ) )

    implicit val `Decoder[Array[Long]]`: Decoder[Array[Long]] = Decoder.instance( _.getLongArray( _ ) )

    implicit def `Decoder[Array[Parcelable]]`[V <: Parcelable: ClassTag]: Decoder[Array[V]] = {
        Decoder[Iterable[V]].map( _.toArray )
    }

    implicit val `Decoder[Array[Short]]`: Decoder[Array[Short]] = Decoder.instance( _.getShortArray( _ ) )

    implicit val `Decoder[Array[String]]`: Decoder[Array[String]] = Decoder.instance( _.getStringArray( _ ) )

    implicit val `Decoder[Boolean]`: Decoder[Boolean] = Decoder.instance( _.getBoolean( _ ) )

    implicit val `Decoder[Bundle]`: Decoder[Bundle] = Decoder.instance( _.getBundle( _ ) )

    implicit val `Decoder[Byte]`: Decoder[Byte] = Decoder.instance( _.getByte( _ ) )

    implicit val `Decoder[Char]`: Decoder[Char] = Decoder.instance( _.getChar( _ ) )

    implicit val `Decoder[CharSequence]`: Decoder[CharSequence] = Decoder.instance( _.getCharSequence( _ ) )

    implicit val `Decoder[Double]`: Decoder[Double] = Decoder.instance( _.getDouble( _ ) )

    implicit def `Decoder[Enumeration]`[V: Enum]: Decoder[V] = Decoder[String].map( Enum[V].decodeOpt( _ ).get )

    implicit val `Decoder[Float]`: Decoder[Float] = Decoder.instance( _.getFloat( _ ) )

    implicit val `Decoder[Int]`: Decoder[Int] = Decoder.instance( _.getInt( _ ) )

    implicit def `Decoder[Iterable[Parcelable]]`[V <: Parcelable, I[V] <: Iterable[V]](
        implicit
        cbf: CanBuildFrom[Nothing, V, I[V]]
    ): Decoder[I[V]] = Decoder.instance {
        case ( bundle, key ) ⇒
            import collection.JavaConversions._
            bundle.getParcelableArrayList[V]( key ).to[I]
    }

    implicit val `Decoder[Long]`: Decoder[Long] = Decoder.instance( _.getLong( _ ) )

    implicit def `Decoder[Option]`[V]( implicit d: Lazy[Decoder[V]] ): Decoder[Option[V]] = new Decoder[Option[V]] {
        override def decode( serialization: ( Bundle, String ) ) = decodeRaw( serialization )

        override def decodeRaw( serialization: ( Bundle, String ) ) = serialization match {
            case ( bundle, key ) ⇒ bundle.containsKey( key ) match {
                case true  ⇒ Option( bundle.read[V]( key )( d.value ) )
                case false ⇒ None
            }
        }
    }

    implicit def `Decoder[Parcelable]`[V <: Parcelable]: Decoder[V] = Decoder.instance( _.getParcelable[V]( _ ) )

    implicit val `Decoder[Short]`: Decoder[Short] = Decoder.instance( _.getShort( _ ) )

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
        Decoder.instance( _.getSparseParcelableArray[V]( _ ) )
    }

    implicit val `Decoder[String]`: Decoder[String] = Decoder.instance( _.getString( _ ) )

    implicit def `Decoder[Traversable]`[V, T[V] <: Traversable[V]](
        implicit
        d:   Lazy[Decoder[Array[V]]],
        cbf: CanBuildFrom[Array[V], V, T[V]]
    ): Decoder[T[V]] = d.map( _.map[T[V]]( _.to[T] ) ).value

    implicit val `Decoder[URL]`: Decoder[URL] = Decoder[String].map( new URL( _ ) )
}

trait Decoders1 extends DecoderOperations {
    implicit def `Decoder[bundler.Encoder]`[V]( implicit d: Lazy[bundler.Decoder[V]] ): Decoder[V] = {
        Decoder.instance{ case ( bundle, key ) ⇒ d.value.decode( bundle.read[Bundle]( key ) ) }
    }
}

trait DecoderOperations {
    def apply[V: Decoder]: Decoder[V] = implicitly[Decoder[V]]

    def instance[V]( f: ( Bundle, String ) ⇒ V ): Decoder[V] = new Decoder[V] {
        override def decodeRaw( serialization: ( Bundle, String ) ) = f.tupled( serialization )
    }

    implicit val `Functor[Decoder]`: Functor[Decoder] = new Functor[Decoder] {
        override def map[A, B]( b: Decoder[A] )( f: A ⇒ B ) = instance {
            case serialization ⇒ f( b.decode( serialization ) )
        }
    }
}