package io.taig.android.parcelable.intent

import java.net.URL

import android.content.Intent
import android.os.Parcelable
import io.taig.android.parcelable
import io.taig.android.parcelable._
import io.taig.android.parcelable.functional._
import io.taig.android.parcelable.syntax._
import io.taig.android.parcelable.util.printBundle
import julienrf.enum.Enum
import shapeless.Lazy

import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import scala.reflect.ClassTag

trait Decoder[V] extends parcelable.Decoder.Guarded[Intent, V] {
    override protected def printHost( intent: Intent ) = printBundle( intent.getExtras )

    override protected def contains( intent: Intent, key: String ) = intent.hasExtra( key )
}

object Decoder extends DecoderOperations with Decoders0

trait Decoders0 extends DecoderOperations with Decoders1 {
    implicit val `Decoder[Array[Boolean]]`: Decoder[Array[Boolean]] = Decoder.instance( _.getBooleanArrayExtra( _ ) )

    implicit val `Decoder[Array[Byte]]`: Decoder[Array[Byte]] = Decoder.instance( _.getByteArrayExtra( _ ) )

    implicit val `Decoder[Array[Char]]`: Decoder[Array[Char]] = Decoder.instance( _.getCharArrayExtra( _ ) )

    implicit val `Decoder[Array[Double]]`: Decoder[Array[Double]] = Decoder.instance( _.getDoubleArrayExtra( _ ) )

    implicit val `Decoder[Array[Float]]`: Decoder[Array[Float]] = Decoder.instance( _.getFloatArrayExtra( _ ) )

    implicit val `Decoder[Array[Int]]`: Decoder[Array[Int]] = Decoder.instance( _.getIntArrayExtra( _ ) )

    implicit val `Decoder[Array[Long]]`: Decoder[Array[Long]] = Decoder.instance( _.getLongArrayExtra( _ ) )

    implicit def `Decoder[Array[Parcelable]]`[V <: Parcelable: ClassTag]: Decoder[Array[V]] = {
        Decoder[Iterable[V]].map( _.toArray )
    }

    implicit val `Decoder[Array[Short]]`: Decoder[Array[Short]] = Decoder.instance( _.getShortArrayExtra( _ ) )

    implicit val `Decoder[Array[String]]`: Decoder[Array[String]] = Decoder.instance( _.getStringArrayExtra( _ ) )

    implicit val `Decoder[Boolean]`: Decoder[Boolean] = Decoder.instance( _.getBooleanExtra( _, false ) )

    implicit val `Decoder[Bundle]`: Decoder[Bundle] = Decoder.instance( _.getBundleExtra( _ ) )

    implicit val `Decoder[Byte]`: Decoder[Byte] = Decoder.instance( _.getByteExtra( _, Byte.MinValue ) )

    implicit val `Decoder[Char]`: Decoder[Char] = Decoder.instance( _.getCharExtra( _, Char.MinValue ) )

    implicit val `Decoder[CharSequence]`: Decoder[CharSequence] = Decoder.instance( _.getCharSequenceExtra( _ ) )

    implicit val `Decoder[Double]`: Decoder[Double] = Decoder.instance( _.getDoubleExtra( _, Double.MinValue ) )

    implicit def `Decoder[Enumeration]`[V: Enum]: Decoder[V] = Decoder[String].map( Enum[V].decodeOpt( _ ).get )

    implicit val `Decoder[Float]`: Decoder[Float] = Decoder.instance( _.getFloatExtra( _, Float.MinValue ) )

    implicit val `Decoder[Int]`: Decoder[Int] = Decoder.instance( _.getIntExtra( _, Int.MaxValue ) )

    implicit def `Decoder[Iterable[Parcelable]]`[V <: Parcelable, I[V] <: Iterable[V]](
        implicit
        cbf: CanBuildFrom[Nothing, V, I[V]]
    ): Decoder[I[V]] = Decoder.instance {
        case ( bundle, key ) ⇒
            import collection.JavaConversions._
            bundle.getParcelableArrayListExtra[V]( key ).to[I]
    }

    implicit val `Decoder[Long]`: Decoder[Long] = Decoder.instance( _.getLongExtra( _, Long.MinValue ) )

    implicit def `Decoder[Option]`[V]( implicit d: Lazy[Decoder[V]] ): Decoder[Option[V]] = new Decoder[Option[V]] {
        override def decode( serialization: ( Intent, String ) ) = decodeRaw( serialization )

        override def decodeRaw( serialization: ( Intent, String ) ) = serialization match {
            case ( intent, key ) ⇒ intent.hasExtra( key ) match {
                case true  ⇒ Option( intent.read[V]( key )( d.value ) )
                case false ⇒ None
            }
        }
    }

    implicit def `Decoder[Parcelable]`[V <: Parcelable]: Decoder[V] = Decoder.instance( _.getParcelableExtra[V]( _ ) )

    implicit val `Decoder[Short]`: Decoder[Short] = Decoder.instance( _.getShortExtra( _, Short.MinValue ) )

    implicit val `Decoder[String]`: Decoder[String] = Decoder.instance( _.getStringExtra( _ ) )

    implicit def `Decoder[Traversable]`[V, T[V] <: Traversable[V]](
        implicit
        d:   Lazy[Decoder[Array[V]]],
        cbf: CanBuildFrom[Array[V], V, T[V]]
    ): Decoder[T[V]] = d.map( _.map[T[V]]( _.to[T] ) ).value

    implicit val `Decoder[URL]`: Decoder[URL] = Decoder[String].map( new URL( _ ) )
}

trait Decoders1 extends DecoderOperations {
    implicit def `Decoder[bundler.Encoder]`[V]( implicit c: Lazy[bundler.Decoder[V]] ): Decoder[V] = {
        Decoder.instance{ case ( intent, key ) ⇒ c.value.decode( intent.read[Bundle]( key ) ) }
    }
}

trait DecoderOperations {
    def apply[V: Decoder]: Decoder[V] = implicitly[Decoder[V]]

    def instance[V]( f: ( Intent, String ) ⇒ V ): Decoder[V] = new Decoder[V] {
        override def decodeRaw( serialization: ( Intent, String ) ) = f.tupled( serialization )
    }

    implicit val `Map[Decoder]`: Functor[Decoder] = new Functor[Decoder] {
        override def map[A, B]( b: Decoder[A] )( f: A ⇒ B ) = instance {
            case serialization ⇒ f( b.decode( serialization ) )
        }
    }
}