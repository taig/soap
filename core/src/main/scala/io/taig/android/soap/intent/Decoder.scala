package io.taig.android.soap.intent

import java.net.URL

import android.content.Intent
import android.os.Parcelable
import cats.Functor
import cats.syntax.functor._
import io.taig.android.soap
import io.taig.android.soap._
import io.taig.android.soap.syntax.intent._
import io.taig.android.soap.util.printBundle
import julienrf.enum.Enum
import shapeless.Lazy

import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import scala.reflect.ClassTag

trait Decoder[V] extends soap.Decoder.Guarded[Intent, V] {
    override protected def printHost( intent: Intent ) = printBundle( intent.getExtras )

    override protected def contains( intent: Intent, key: String ) = intent.hasExtra( key )
}

object Decoder extends DecoderOperations with Decoders0

trait Decoders0 extends DecoderOperations with Decoders1 {
    implicit val decoderArrayBoolean: Decoder[Array[Boolean]] = Decoder.instance( _.getBooleanArrayExtra( _ ) )

    implicit val decoderArrayByte: Decoder[Array[Byte]] = Decoder.instance( _.getByteArrayExtra( _ ) )

    implicit val decoderArrayChar: Decoder[Array[Char]] = Decoder.instance( _.getCharArrayExtra( _ ) )

    implicit val decoderArrayDouble: Decoder[Array[Double]] = Decoder.instance( _.getDoubleArrayExtra( _ ) )

    implicit val decoderArrayFloat: Decoder[Array[Float]] = Decoder.instance( _.getFloatArrayExtra( _ ) )

    implicit val decoderArrayInt: Decoder[Array[Int]] = Decoder.instance( _.getIntArrayExtra( _ ) )

    implicit val decoderArrayLong: Decoder[Array[Long]] = Decoder.instance( _.getLongArrayExtra( _ ) )

    implicit def decoderArrayParcelable[V <: Parcelable: ClassTag]: Decoder[Array[V]] = {
        Decoder[Iterable[V]].map( _.toArray )
    }

    implicit val decoderArrayShort: Decoder[Array[Short]] = Decoder.instance( _.getShortArrayExtra( _ ) )

    implicit val decoderArrayString: Decoder[Array[String]] = Decoder.instance( _.getStringArrayExtra( _ ) )

    implicit val decoderBoolean: Decoder[Boolean] = Decoder.instance( _.getBooleanExtra( _, false ) )

    implicit val decoderBundle: Decoder[Bundle] = Decoder.instance( _.getBundleExtra( _ ) )

    implicit val decoderByte: Decoder[Byte] = Decoder.instance( _.getByteExtra( _, Byte.MinValue ) )

    implicit val decoderChar: Decoder[Char] = Decoder.instance( _.getCharExtra( _, Char.MinValue ) )

    implicit val decoderCharSequence: Decoder[CharSequence] = Decoder.instance( _.getCharSequenceExtra( _ ) )

    implicit val decoderDouble: Decoder[Double] = Decoder.instance( _.getDoubleExtra( _, Double.MinValue ) )

    implicit def decoderEnumeration[V: Enum.Derived]: Decoder[V] = Decoder[String].map( Enum[V].decodeOpt( _ ).get )

    implicit val decoderFloat: Decoder[Float] = Decoder.instance( _.getFloatExtra( _, Float.MinValue ) )

    implicit val decoderInt: Decoder[Int] = Decoder.instance( _.getIntExtra( _, Int.MaxValue ) )

    implicit def decoderIterableParcelable[V <: Parcelable, I[V] <: Iterable[V]](
        implicit
        cbf: CanBuildFrom[Nothing, V, I[V]]
    ): Decoder[I[V]] = Decoder.instance {
        case ( bundle, key ) ⇒
            import collection.JavaConversions._
            bundle.getParcelableArrayListExtra[V]( key ).to[I]
    }

    implicit val decoderLong: Decoder[Long] = Decoder.instance( _.getLongExtra( _, Long.MinValue ) )

    implicit def decoderOption[V]( implicit d: Lazy[Decoder[V]] ): Decoder[Option[V]] = new Decoder[Option[V]] {
        override def decode( serialization: ( Intent, String ) ) = decodeRaw( serialization )

        override def decodeRaw( serialization: ( Intent, String ) ) = serialization match {
            case ( intent, key ) ⇒ intent.hasExtra( key ) match {
                case true  ⇒ Option( intent.read[V]( key )( d.value ) )
                case false ⇒ None
            }
        }
    }

    implicit def decoderParcelable[V <: Parcelable]: Decoder[V] = Decoder.instance( _.getParcelableExtra[V]( _ ) )

    implicit val decoderShort: Decoder[Short] = Decoder.instance( _.getShortExtra( _, Short.MinValue ) )

    implicit val decoderString: Decoder[String] = Decoder.instance( _.getStringExtra( _ ) )

    implicit def decoderTraversable[V, T[V] <: Traversable[V]](
        implicit
        d:   Lazy[Decoder[Array[V]]],
        cbf: CanBuildFrom[Array[V], V, T[V]]
    ): Decoder[T[V]] = d.map( _.map[T[V]]( _.to[T] ) ).value

    implicit val decoderURL: Decoder[URL] = Decoder[String].map( new URL( _ ) )
}

trait Decoders1 extends DecoderOperations {
    implicit def decoderBundlerEncoder[V]( implicit c: Lazy[bundler.Decoder[V]] ): Decoder[V] = {
        Decoder.instance{ case ( intent, key ) ⇒ c.value.decode( intent.read[Bundle]( key ) ) }
    }
}

trait DecoderOperations {
    def apply[V: Decoder]: Decoder[V] = implicitly[Decoder[V]]

    def instance[V]( f: ( Intent, String ) ⇒ V ): Decoder[V] = new Decoder[V] {
        override def decodeRaw( serialization: ( Intent, String ) ) = f.tupled( serialization )
    }

    implicit val mapDecoder: Functor[Decoder] = new Functor[Decoder] {
        override def map[A, B]( b: Decoder[A] )( f: A ⇒ B ) = instance {
            case serialization ⇒ f( b.decode( serialization ) )
        }
    }
}