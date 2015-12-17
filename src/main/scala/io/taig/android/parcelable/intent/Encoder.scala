package io.taig.android.parcelable.intent

import java.net.URL

import android.content.Intent
import android.os.Parcelable
import io.taig.android.parcelable
import io.taig.android.parcelable._
import io.taig.android.parcelable.functional._
import io.taig.android.parcelable.syntax._
import julienrf.enum.Enum
import shapeless.Lazy

import scala.language.higherKinds
import scala.reflect.ClassTag

trait Encoder[V] extends parcelable.Encoder[( Intent, String, V ), Unit]

object Encoder extends EncoderOperations with Encoders0

trait Encoders0 extends EncoderOperations with Encoders1 {
    implicit val `Encoder[Array[Boolean]]`: Encoder[Array[Boolean]] = Encoder.instance( _.putExtra( _, _ ) )

    implicit val `Encoder[Array[Byte]]`: Encoder[Array[Byte]] = Encoder.instance( _.putExtra( _, _ ) )

    implicit val `Encoder[Array[Char]]`: Encoder[Array[Char]] = Encoder.instance( _.putExtra( _, _ ) )

    implicit val `Encoder[Array[Double]]`: Encoder[Array[Double]] = Encoder.instance( _.putExtra( _, _ ) )

    implicit val `Encoder[Array[Float]]`: Encoder[Array[Float]] = Encoder.instance( _.putExtra( _, _ ) )

    implicit val `Encoder[Array[Int]]`: Encoder[Array[Int]] = Encoder.instance( _.putExtra( _, _ ) )

    implicit val `Encoder[Array[Long]]`: Encoder[Array[Long]] = Encoder.instance( _.putExtra( _, _ ) )

    implicit def `Encoder[Array[Parcelable]]`[V <: Parcelable]: Encoder[Array[V]] = {
        Encoder[Iterable[V]].contramap( _.toIterable )
    }

    implicit val `Encoder[Array[Short]]`: Encoder[Array[Short]] = Encoder.instance( _.putExtra( _, _ ) )

    implicit val `Encoder[Array[String]]`: Encoder[Array[String]] = Encoder.instance( _.putExtra( _, _ ) )

    implicit val `Encoder[Boolean]`: Encoder[Boolean] = Encoder.instance( _.putExtra( _, _ ) )

    implicit val `Encoder[Bundle]`: Encoder[Bundle] = Encoder.instance( _.putExtra( _, _ ) )

    implicit val `Encoder[Byte]`: Encoder[Byte] = Encoder.instance( _.putExtra( _, _ ) )

    implicit val `Encoder[Char]`: Encoder[Char] = Encoder.instance( _.putExtra( _, _ ) )

    implicit val `Encoder[CharSequence]`: Encoder[CharSequence] = Encoder.instance( _.putExtra( _, _ ) )

    implicit val `Encoder[Double]`: Encoder[Double] = Encoder.instance( _.putExtra( _, _ ) )

    implicit def `Encoder[Enumeration]`[V: Enum]: Encoder[V] = Encoder[String].contramap( Enum[V].encode )

    implicit val `Encoder[Float]`: Encoder[Float] = Encoder.instance( _.putExtra( _, _ ) )

    implicit val `Encoder[Int]`: Encoder[Int] = Encoder.instance( _.putExtra( _, _ ) )

    implicit val `Encoder[Long]`: Encoder[Long] = Encoder.instance( _.putExtra( _, _ ) )

    implicit def `Encoder[Parcelable]`[V <: Parcelable]: Encoder[V] = Encoder.instance( _.putExtra( _, _ ) )

    implicit def `Encoder[Option]`[V]( implicit e: Lazy[Encoder[V]] ): Encoder[Option[V]] = Encoder.instance {
        case ( intent, key, value ) ⇒ value.foreach( intent.write( key, _ )( e.value ) )
    }

    implicit def `Encoder[Iterable[Parcelable]]`[V <: Parcelable, I[V] <: Iterable[V]]: Encoder[I[V]] = {
        Encoder.instance { ( bundle, key, value ) ⇒
            import collection.JavaConversions._
            bundle.putParcelableArrayListExtra( key, new java.util.ArrayList[V]( value ) )
        }
    }

    implicit val `Encoder[Short]`: Encoder[Short] = Encoder.instance( _.putExtra( _, _ ) )

    implicit def `Encoder[Traversable]`[V: ClassTag, T[V] <: Traversable[V]](
        implicit
        e: Lazy[Encoder[Array[V]]]
    ): Encoder[T[V]] = e.map( _.contramap[T[V]]( _.toArray ) ).value

    implicit val `Encoder[String]`: Encoder[String] = Encoder.instance( _.putExtra( _, _ ) )

    implicit val `Encoder[URL]`: Encoder[URL] = Encoder[String].contramap( _.toString )
}

trait Encoders1 extends EncoderOperations {
    implicit def `Encoder[bundler.Encoder]`[V]( implicit e: Lazy[bundler.Encoder[V]] ): Encoder[V] = Encoder.instance {
        case ( intent, key, value ) ⇒ intent.write( key, e.value.encode( value ) )
    }
}

trait EncoderOperations {
    def apply[V: Encoder]: Encoder[V] = implicitly[Encoder[V]]

    def instance[V]( f: ( Intent, String, V ) ⇒ Unit ): Encoder[V] = new Encoder[V] {
        override def encode( value: ( Intent, String, V ) ) = f.tupled( value )
    }

    implicit val `Contramap[Bundle]`: Contramap[Encoder] = new Contramap[Encoder] {
        override def contramap[A, B]( b: Encoder[A] )( f: B ⇒ A ) = new Encoder[B] {
            override def encode( value: ( Intent, String, B ) ) = b.encode( value.copy( _3 = f( value._3 ) ) )
        }
    }
}