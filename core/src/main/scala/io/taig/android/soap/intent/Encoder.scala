package io.taig.android.soap.intent

import java.net.URL

import android.content.Intent
import android.os.Parcelable
import io.taig.android.soap
import io.taig.android.soap._
import io.taig.android.soap.functional._
import io.taig.android.soap.syntax.functional._
import io.taig.android.soap.syntax.intent._
import julienrf.enum.Enum
import shapeless.Lazy

import scala.language.higherKinds
import scala.reflect.ClassTag

trait Encoder[V] extends soap.Encoder[( Intent, String, V ), Unit]

object Encoder extends EncoderOperations with Encoders0

trait Encoders0 extends EncoderOperations with Encoders1 {
    implicit val encoderArrayBoolean: Encoder[Array[Boolean]] = Encoder.instance( _.putExtra( _, _ ) )

    implicit val encoderArrayByte: Encoder[Array[Byte]] = Encoder.instance( _.putExtra( _, _ ) )

    implicit val encoderArrayChar: Encoder[Array[Char]] = Encoder.instance( _.putExtra( _, _ ) )

    implicit val encoderArrayDouble: Encoder[Array[Double]] = Encoder.instance( _.putExtra( _, _ ) )

    implicit val encoderArrayFloat: Encoder[Array[Float]] = Encoder.instance( _.putExtra( _, _ ) )

    implicit val encoderArrayInt: Encoder[Array[Int]] = Encoder.instance( _.putExtra( _, _ ) )

    implicit val encoderArrayLong: Encoder[Array[Long]] = Encoder.instance( _.putExtra( _, _ ) )

    implicit def encoderArrayParcelable[V <: Parcelable: ClassTag]: Encoder[Array[V]] = {
        Encoder[Iterable[V]].contramap( _.toIterable )
    }

    implicit val encoderArrayShort: Encoder[Array[Short]] = Encoder.instance( _.putExtra( _, _ ) )

    implicit val encoderArrayString: Encoder[Array[String]] = Encoder.instance( _.putExtra( _, _ ) )

    implicit val encoderBoolean: Encoder[Boolean] = Encoder.instance( _.putExtra( _, _ ) )

    implicit val encoderBundle: Encoder[Bundle] = Encoder.instance( _.putExtra( _, _ ) )

    implicit val encoderByte: Encoder[Byte] = Encoder.instance( _.putExtra( _, _ ) )

    implicit val encoderChar: Encoder[Char] = Encoder.instance( _.putExtra( _, _ ) )

    implicit val encoderCharSequence: Encoder[CharSequence] = Encoder.instance( _.putExtra( _, _ ) )

    implicit val encoderDouble: Encoder[Double] = Encoder.instance( _.putExtra( _, _ ) )

    implicit def encoderEnumeration[V: Enum.Derived]: Encoder[V] = Encoder[String].contramap( Enum[V].encode )

    implicit val encoderFloat: Encoder[Float] = Encoder.instance( _.putExtra( _, _ ) )

    implicit val encoderInt: Encoder[Int] = Encoder.instance( _.putExtra( _, _ ) )

    implicit def encoderIterableParcelable[V <: Parcelable, I[V] <: Iterable[V]]: Encoder[I[V]] = {
        Encoder.instance {
            case ( bundle, key, value ) ⇒
                import collection.JavaConversions._
                bundle.putParcelableArrayListExtra( key, new java.util.ArrayList[V]( value ) )
        }
    }

    implicit val encoderLong: Encoder[Long] = Encoder.instance( _.putExtra( _, _ ) )

    implicit def encoderOption[V]( implicit c: Lazy[Encoder[V]] ): Encoder[Option[V]] = Encoder.instance {
        case ( intent, key, value ) ⇒ value.foreach( intent.write( key, _ )( c.value ) )
    }

    implicit def encoderParcelable[V <: Parcelable]: Encoder[V] = Encoder.instance( _.putExtra( _, _ ) )

    implicit val encoderShort: Encoder[Short] = Encoder.instance( _.putExtra( _, _ ) )

    implicit val encoderString: Encoder[String] = Encoder.instance( _.putExtra( _, _ ) )

    implicit def encoderTraversable[V: ClassTag, T[V] <: Traversable[V]](
        implicit
        e: Lazy[Encoder[Array[V]]]
    ): Encoder[T[V]] = e.map( _.contramap[T[V]]( _.toArray ) ).value

    implicit val encoderURL: Encoder[URL] = Encoder[String].contramap( _.toString )
}

trait Encoders1 extends EncoderOperations {
    implicit def encoderBundlerEncoder[V]( implicit e: Lazy[bundler.Encoder[V]] ): Encoder[V] = {
        Encoder.instance{ case ( intent, key, value ) ⇒ intent.write[Bundle]( key, e.value.encode( value ) ) }
    }
}

trait EncoderOperations {
    def apply[V: Encoder]: Encoder[V] = implicitly[Encoder[V]]

    def instance[V]( f: ( Intent, String, V ) ⇒ Unit ): Encoder[V] = new Encoder[V] {
        override def encode( value: ( Intent, String, V ) ) = f.tupled( value )
    }

    implicit val contramapEncoder: Contravariant[Encoder] = new Contravariant[Encoder] {
        override def contramap[A, B]( b: Encoder[A] )( f: B ⇒ A ) = instance {
            case ( intent, key, value ) ⇒ b.encode( intent, key, f( value ) )
        }
    }
}