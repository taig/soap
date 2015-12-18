package io.taig.android.parcelable.intent

import java.net.URL

import android.content.Intent
import android.os.Parcelable
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
    extends parcelable.Codec[( Intent, String, V ), Unit, ( Intent, String ), V]
    with Encoder[V]
    with Decoder[V]

@exports
object Codec extends CodecOperations with Codecs0

trait Codecs0 extends CodecOperations with Codecs1 {
    implicit val `Codec[Array[Boolean]]`: Codec[Array[Boolean]] = Codec.instance(
        _.putExtra( _, _ ),
        _.getBooleanArrayExtra( _ )
    )

    implicit val `Codec[Array[Byte]]`: Codec[Array[Byte]] = Codec.instance(
        _.putExtra( _, _ ),
        _.getByteArrayExtra( _ )
    )

    implicit val `Codec[Array[Char]]`: Codec[Array[Char]] = Codec.instance(
        _.putExtra( _, _ ),
        _.getCharArrayExtra( _ )
    )

    implicit val `Codec[Array[Double]]`: Codec[Array[Double]] = Codec.instance(
        _.putExtra( _, _ ),
        _.getDoubleArrayExtra( _ )
    )

    implicit val `Codec[Array[Float]]`: Codec[Array[Float]] = Codec.instance(
        _.putExtra( _, _ ),
        _.getFloatArrayExtra( _ )
    )

    implicit val `Codec[Array[Int]]`: Codec[Array[Int]] = Codec.instance(
        _.putExtra( _, _ ),
        _.getIntArrayExtra( _ )
    )

    implicit val `Codec[Array[Long]]`: Codec[Array[Long]] = Codec.instance(
        _.putExtra( _, _ ),
        _.getLongArrayExtra( _ )
    )

    implicit def `Codec[Array[Parcelable]]`[V <: Parcelable: ClassTag]: Codec[Array[V]] = Codec[Iterable[V]].inmap(
        _.toIterable,
        _.toArray
    )

    implicit val `Codec[Array[Short]]`: Codec[Array[Short]] = Codec.instance(
        _.putExtra( _, _ ),
        _.getShortArrayExtra( _ )
    )

    implicit val `Codec[Array[String]]`: Codec[Array[String]] = Codec.instance(
        _.putExtra( _, _ ),
        _.getStringArrayExtra( _ )
    )

    implicit val `Codec[Boolean]`: Codec[Boolean] = Codec.instance(
        _.putExtra( _, _ ),
        _.getBooleanExtra( _, false )
    )

    implicit val `Codec[Bundle]`: Codec[Bundle] = Codec.instance(
        _.putExtra( _, _ ),
        _.getBundleExtra( _ )
    )

    implicit val `Codec[Byte]`: Codec[Byte] = Codec.instance(
        _.putExtra( _, _ ),
        _.getByteExtra( _, Byte.MinValue )
    )

    implicit val `Codec[Char]`: Codec[Char] = Codec.instance(
        _.putExtra( _, _ ),
        _.getCharExtra( _, Char.MinValue )
    )

    implicit val `Codec[CharSequence]`: Codec[CharSequence] = Codec.instance(
        _.putExtra( _, _ ),
        _.getCharSequenceExtra( _ )
    )

    implicit val `Codec[Double]`: Codec[Double] = Codec.instance(
        _.putExtra( _, _ ),
        _.getDoubleExtra( _, Double.MinValue )
    )

    implicit def `Codec[Enumeration]`[V: Enum]: Codec[V] = Codec[String].inmap(
        Enum[V].encode,
        Enum[V].decodeOpt( _ ).get
    )

    implicit val `Codec[Float]`: Codec[Float] = Codec.instance(
        _.putExtra( _, _ ),
        _.getFloatExtra( _, Float.MinValue )
    )

    implicit val `Codec[Int]`: Codec[Int] = Codec.instance(
        _.putExtra( _, _ ),
        _.getIntExtra( _, Int.MaxValue )
    )

    implicit def `Codec[Iterable[Parcelable]]`[V <: Parcelable, I[V] <: Iterable[V]](
        implicit
        cbf: CanBuildFrom[Nothing, V, I[V]]
    ): Codec[I[V]] = {
        import collection.JavaConversions._

        Codec.instance(
            ( bundle, key, value ) ⇒ bundle.putParcelableArrayListExtra( key, new java.util.ArrayList[V]( value ) ),
            ( bundle, key ) ⇒ bundle.getParcelableArrayListExtra[V]( key ).to[I]
        )
    }

    implicit val `Codec[Long]`: Codec[Long] = Codec.instance(
        _.putExtra( _, _ ),
        _.getLongExtra( _, Long.MinValue )
    )

    implicit def `Codec[Option]`[V]( implicit c: Lazy[Codec[V]] ): Codec[Option[V]] = new Codec[Option[V]] {
        override def encode( value: ( Intent, String, Option[V] ) ) = value match {
            case ( intent, key, value ) ⇒ value.foreach( intent.write( key, _ )( c.value ) )
        }

        override def decode( serialization: ( Intent, String ) ) = decodeRaw( serialization )

        override def decodeRaw( serialization: ( Intent, String ) ) = serialization match {
            case ( intent, key ) ⇒ intent.hasExtra( key ) match {
                case true  ⇒ Option( intent.read[V]( key )( c.value ) )
                case false ⇒ None
            }
        }
    }

    implicit def `Codec[Parcelable]`[V <: Parcelable]: Codec[V] = Codec.instance(
        _.putExtra( _, _ ),
        _.getParcelableExtra[V]( _ )
    )

    implicit val `Codec[Short]`: Codec[Short] = Codec.instance(
        _.putExtra( _, _ ),
        _.getShortExtra( _, Short.MinValue )
    )

    implicit val `Codec[String]`: Codec[String] = Codec.instance(
        _.putExtra( _, _ ),
        _.getStringExtra( _ )
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
            { case ( intent, key, value ) ⇒ intent.write[Bundle]( key, c.value.encode( value ) ) },
            { case ( intent, key ) ⇒ c.value.decode( intent.read[Bundle]( key ) ) }
        )
    }
}

trait CodecOperations {
    def apply[V: Codec]: Codec[V] = implicitly[Codec[V]]

    def instance[V]( e: ( Intent, String, V ) ⇒ Unit, d: ( Intent, String ) ⇒ V ): Codec[V] = new Codec[V] {
        override def encode( value: ( Intent, String, V ) ) = e.tupled( value )

        override def decodeRaw( serialization: ( Intent, String ) ) = d.tupled( serialization )
    }

    implicit val `Inmap[Codec]`: Inmap[Codec] = new Inmap[Codec] {
        override def inmap[A, B]( fa: Codec[A] )( contramap: B ⇒ A, map: A ⇒ B ) = instance(
            { case value ⇒ implicitly[Contravariant[Encoder]].contramap( fa )( contramap ).encode( value ) },
            { case serialization ⇒ implicitly[Functor[Decoder]].map( fa )( map ).decode( serialization ) }
        )
    }
}