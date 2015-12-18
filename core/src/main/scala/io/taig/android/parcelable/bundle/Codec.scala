package io.taig.android.parcelable.bundle

import java.net.URL

import android.annotation.TargetApi
import android.os.Parcelable
import android.util.{ Size, SizeF, SparseArray }
import export.exports
import io.taig.android.parcelable
import io.taig.android.parcelable._
import io.taig.android.parcelable.functional._
import io.taig.android.parcelable.syntax._
import julienrf.enum.Enum
import shapeless.Lazy

import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import scala.reflect.ClassTag

trait Codec[V]
    extends parcelable.Codec[( Bundle, String, V ), Unit, ( Bundle, String ), V]
    with Encoder[V]
    with Decoder[V]

@exports
object Codec extends CodecOperations with Codecs0

trait Codecs0 extends CodecOperations with Codecs1 {
    implicit val `Codec[Array[Boolean]]`: Codec[Array[Boolean]] = Codec.instance(
        _.putBooleanArray( _, _ ),
        _.getBooleanArray( _ )
    )

    implicit val `Codec[Array[Byte]]`: Codec[Array[Byte]] = Codec.instance(
        _.putByteArray( _, _ ),
        _.getByteArray( _ )
    )

    implicit val `Codec[Array[Char]]`: Codec[Array[Char]] = Codec.instance(
        _.putCharArray( _, _ ),
        _.getCharArray( _ )
    )

    implicit val `Codec[Array[Double]]`: Codec[Array[Double]] = Codec.instance(
        _.putDoubleArray( _, _ ),
        _.getDoubleArray( _ )
    )

    implicit val `Codec[Array[Float]]`: Codec[Array[Float]] = Codec.instance(
        _.putFloatArray( _, _ ),
        _.getFloatArray( _ )
    )

    implicit val `Codec[Array[Int]]`: Codec[Array[Int]] = Codec.instance(
        _.putIntArray( _, _ ),
        _.getIntArray( _ )
    )

    implicit val `Codec[Array[Long]]`: Codec[Array[Long]] = Codec.instance(
        _.putLongArray( _, _ ),
        _.getLongArray( _ )
    )

    implicit def `Codec[Array[Parcelable]]`[V <: Parcelable: ClassTag]: Codec[Array[V]] = Codec[Iterable[V]].inmap(
        _.toIterable,
        _.toArray
    )

    implicit val `Codec[Array[Short]]`: Codec[Array[Short]] = Codec.instance(
        _.putShortArray( _, _ ),
        _.getShortArray( _ )
    )

    implicit val `Codec[Array[String]]`: Codec[Array[String]] = Codec.instance(
        _.putStringArray( _, _ ),
        _.getStringArray( _ )
    )

    implicit val `Codec[Boolean]`: Codec[Boolean] = Codec.instance(
        _.putBoolean( _, _ ),
        _.getBoolean( _ )
    )

    implicit val `Codec[Bundle]`: Codec[Bundle] = Codec.instance(
        _.putBundle( _, _ ),
        _.getBundle( _ )
    )

    implicit val `Codec[Byte]`: Codec[Byte] = Codec.instance(
        _.putByte( _, _ ),
        _.getByte( _ )
    )

    implicit val `Codec[Char]`: Codec[Char] = Codec.instance(
        _.putChar( _, _ ),
        _.getChar( _ )
    )

    implicit val `Codec[CharSequence]`: Codec[CharSequence] = Codec.instance(
        _.putCharSequence( _, _ ),
        _.getCharSequence( _ )
    )

    implicit val `Codec[Double]`: Codec[Double] = Codec.instance(
        _.putDouble( _, _ ),
        _.getDouble( _ )
    )

    implicit def `Codec[Enumeration]`[V: Enum]: Codec[V] = Codec[String].inmap(
        Enum[V].encode,
        Enum[V].decodeOpt( _ ).get
    )

    implicit val `Codec[Float]`: Codec[Float] = Codec.instance(
        _.putFloat( _, _ ),
        _.getFloat( _ )
    )

    implicit val `Codec[Int]`: Codec[Int] = Codec.instance(
        _.putInt( _, _ ),
        _.getInt( _ )
    )

    implicit def `Codec[Iterable[Parcelable]]`[V <: Parcelable, I[V] <: Iterable[V]](
        implicit
        cbf: CanBuildFrom[Nothing, V, I[V]]
    ): Codec[I[V]] = {
        import collection.JavaConversions._

        Codec.instance(
            ( bundle, key, value ) ⇒ bundle.putParcelableArrayList( key, new java.util.ArrayList[V]( value ) ),
            ( bundle, key ) ⇒ bundle.getParcelableArrayList[V]( key ).to[I]
        )
    }

    implicit val `Codec[Long]`: Codec[Long] = Codec.instance(
        _.putLong( _, _ ),
        _.getLong( _ )
    )

    implicit def `Codec[Option]`[V]( implicit c: Lazy[Codec[V]] ): Codec[Option[V]] = new Codec[Option[V]] {
        override def encode( value: ( Bundle, String, Option[V] ) ) = value match {
            case ( bundle, key, value ) ⇒ value.foreach( bundle.write( key, _ )( c.value ) )
        }

        override def decode( serialization: ( Bundle, String ) ) = decodeRaw( serialization )

        override def decodeRaw( serialization: ( Bundle, String ) ) = serialization match {
            case ( bundle, key ) ⇒ bundle.containsKey( key ) match {
                case true  ⇒ Option( bundle.read[V]( key )( c.value ) )
                case false ⇒ None
            }
        }
    }

    implicit def `Codec[Parcelable]`[V <: Parcelable]: Codec[V] = Codec.instance(
        _.putParcelable( _, _ ),
        _.getParcelable[V]( _ )
    )

    implicit val `Codec[Short]`: Codec[Short] = Codec.instance(
        _.putShort( _, _ ),
        _.getShort( _ )
    )

    implicit val `Codec[Size]`: Codec[Size] = new Codec[Size] {
        @TargetApi( 21 )
        override def encode( value: ( Bundle, String, Size ) ) = value match {
            case ( bundle, key, value ) ⇒ bundle.putSize( key, value )
        }

        @TargetApi( 21 )
        override def decodeRaw( serialization: ( Bundle, String ) ) = serialization match {
            case ( bundle, key ) ⇒ bundle.getSize( key )
        }
    }

    implicit val `Codec[SizeF]`: Codec[SizeF] = new Codec[SizeF] {
        @TargetApi( 21 )
        override def encode( value: ( Bundle, String, SizeF ) ) = value match {
            case ( bundle, key, value ) ⇒ bundle.putSizeF( key, value )
        }

        @TargetApi( 21 )
        override def decodeRaw( serialization: ( Bundle, String ) ) = serialization match {
            case ( bundle, key ) ⇒ bundle.getSizeF( key )
        }
    }

    implicit def `Codec[SparseArray[Parcelable]]`[V <: Parcelable]: Codec[SparseArray[V]] = Codec.instance(
        _.putSparseParcelableArray( _, _ ),
        _.getSparseParcelableArray[V]( _ )
    )

    implicit val `Codec[String]`: Codec[String] = Codec.instance(
        _.putString( _, _ ),
        _.getString( _ )
    )

    implicit def `Codec[Traversable]`[V: ClassTag, T[V] <: Traversable[V]](
        implicit
        c:   Lazy[Codec[Array[V]]],
        cbf: CanBuildFrom[Array[V], V, T[V]]
    ): Codec[T[V]] = c.map( _.inmap[T[V]]( _.toArray, _.to[T] ) ).value

    implicit val `Codec[URL]`: Codec[URL] = Codec[String].inmap( _.toString, new URL( _ ) )
}

trait Codecs1 extends CodecOperations {
    implicit def `Codec[bundler.Encoder]`[V]( implicit c: Lazy[bundler.Codec[V]] ): Codec[V] = {
        import codecs._

        Codec.instance(
            { case ( bundle, key, value ) ⇒ bundle.write[Bundle]( key, c.value.encode( value ) ) },
            { case ( bundle, key ) ⇒ c.value.decode( bundle.read[Bundle]( key ) ) }
        )
    }
}

trait CodecOperations {
    def apply[V: Codec]: Codec[V] = implicitly[Codec[V]]

    def instance[V]( e: ( Bundle, String, V ) ⇒ Unit, d: ( Bundle, String ) ⇒ V ): Codec[V] = new Codec[V] {
        override def encode( value: ( Bundle, String, V ) ) = e.tupled( value )

        override def decodeRaw( serialization: ( Bundle, String ) ) = d.tupled( serialization )
    }

    implicit val `Inmap[Codec]`: Inmap[Codec] = new Inmap[Codec] {
        override def inmap[A, B]( fa: Codec[A] )( contramap: B ⇒ A, map: A ⇒ B ) = instance(
            { case value ⇒ implicitly[Contravariant[Encoder]].contramap( fa )( contramap ).encode( value ) },
            { case serialization ⇒ implicitly[Functor[Decoder]].map( fa )( map ).decode( serialization ) }
        )
    }
}