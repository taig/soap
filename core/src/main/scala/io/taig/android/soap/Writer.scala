package io.taig.android.soap

import java.io.Serializable
import java.net.URL
import java.util.ArrayList

import android.content.{ Intent ⇒ AIntent }
import android.os.{ Parcelable, Bundle ⇒ ABundle }
import cats.functor.Contravariant
import cats.syntax.contravariant._
import enum.Enum
import shapeless.Lazy

import scala.language.{ higherKinds, implicitConversions }
import scala.reflect.ClassTag

/**
 * Type class which describes how to write a value V into a container C
 */
trait Writer[C, V] {
    def write( container: C, key: String, value: V ): Unit
}

object Writer extends Writer0 {
    type Bundle[V] = Writer[ABundle, V]

    type Intent[V] = Writer[AIntent, V]
}

trait Writer0 extends Writer1 {
    implicit val writerBundleArrayBoolean: Writer.Bundle[Array[Boolean]] = instance( _.putBooleanArray( _, _ ) )

    implicit val writerBundleArrayByte: Writer.Bundle[Array[Byte]] = instance( _.putByteArray( _, _ ) )

    implicit val writerBundleArrayChar: Writer.Bundle[Array[Char]] = instance( _.putCharArray( _, _ ) )

    implicit val writerBundleArrayDouble: Writer.Bundle[Array[Double]] = instance( _.putDoubleArray( _, _ ) )

    implicit val writerBundleArrayFloat: Writer.Bundle[Array[Float]] = instance( _.putFloatArray( _, _ ) )

    implicit val writerBundleArrayInt: Writer.Bundle[Array[Int]] = instance( _.putIntArray( _, _ ) )

    implicit val writerBundleArrayLong: Writer.Bundle[Array[Long]] = instance( _.putLongArray( _, _ ) )

    implicit val writerBundleArrayShort: Writer.Bundle[Array[Short]] = instance( _.putShortArray( _, _ ) )

    implicit val writerBundleArrayString: Writer.Bundle[Array[String]] = instance( _.putStringArray( _, _ ) )

    implicit val writerBundleBoolean: Writer.Bundle[Boolean] = instance( _.putBoolean( _, _ ) )

    implicit val writerBundleBundle: Writer.Bundle[Bundle] = instance( _.putBundle( _, _ ) )

    implicit val writerBundleByte: Writer.Bundle[Byte] = instance( _.putByte( _, _ ) )

    implicit val writerBundleChar: Writer.Bundle[Char] = instance( _.putChar( _, _ ) )

    implicit val writerBundleCharSequence: Writer.Bundle[CharSequence] = instance( _.putCharSequence( _, _ ) )

    implicit val writerBundleDouble: Writer.Bundle[Double] = instance( _.putDouble( _, _ ) )

    implicit val writerBundleFloat: Writer.Bundle[Float] = instance( _.putFloat( _, _ ) )

    implicit val writerBundleInt: Writer.Bundle[Int] = instance( _.putInt( _, _ ) )

    implicit def writerBundleIterableParcelable[V <: Parcelable, F[α] <: Iterable[α]]: Writer.Bundle[F[V]] = {
        instance { ( bundle, key, value ) ⇒
            import scala.collection.JavaConversions._
            bundle.putParcelableArrayList( key, new ArrayList( value ) )
        }
    }

    implicit val writerBundleLong: Writer.Bundle[Long] = instance( _.putLong( _, _ ) )

    implicit def writerBundleParcelable[V <: Parcelable]: Writer.Bundle[V] = instance( _.putParcelable( _, _ ) )

    def writerBundleSerializable[V <: Serializable]: Writer.Bundle[V] = instance( _.putSerializable( _, _ ) )

    implicit val writerBundleShort: Writer.Bundle[Short] = instance( _.putShort( _, _ ) )

    implicit val writerBundleString: Writer.Bundle[String] = instance( _.putString( _, _ ) )

    implicit val writerIntentArrayBoolean: Writer.Intent[Array[Boolean]] = instance( _.putExtra( _, _ ) )

    implicit val writerIntentArrayByte: Writer.Intent[Array[Byte]] = instance( _.putExtra( _, _ ) )

    implicit val writerIntentArrayChar: Writer.Intent[Array[Char]] = instance( _.putExtra( _, _ ) )

    implicit val writerIntentArrayDouble: Writer.Intent[Array[Double]] = instance( _.putExtra( _, _ ) )

    implicit val writerIntentArrayFloat: Writer.Intent[Array[Float]] = instance( _.putExtra( _, _ ) )

    implicit val writerIntentArrayInt: Writer.Intent[Array[Int]] = instance( _.putExtra( _, _ ) )

    implicit val writerIntentArrayLong: Writer.Intent[Array[Long]] = instance( _.putExtra( _, _ ) )

    implicit val writerIntentArrayShort: Writer.Intent[Array[Short]] = instance( _.putExtra( _, _ ) )

    implicit val writerIntentArrayString: Writer.Intent[Array[String]] = instance( _.putExtra( _, _ ) )

    implicit val writerIntentBoolean: Writer.Intent[Boolean] = instance( _.putExtra( _, _ ) )

    implicit val writerIntentBundle: Writer.Intent[Bundle] = instance( _.putExtra( _, _ ) )

    implicit val writerIntentByte: Writer.Intent[Byte] = instance( _.putExtra( _, _ ) )

    implicit val writerIntentChar: Writer.Intent[Char] = instance( _.putExtra( _, _ ) )

    implicit val writerIntentCharSequence: Writer.Intent[CharSequence] = instance( _.putExtra( _, _ ) )

    implicit val writerIntentDouble: Writer.Intent[Double] = instance( _.putExtra( _, _ ) )

    implicit val writerIntentFloat: Writer.Intent[Float] = instance( _.putExtra( _, _ ) )

    implicit val writerIntentInt: Writer.Intent[Int] = instance( _.putExtra( _, _ ) )

    implicit def writerIntentIterableParcelable[V <: Parcelable, F[α] <: Iterable[α]]: Writer.Intent[F[V]] = {
        instance { ( intent, key, value ) ⇒
            import scala.collection.JavaConversions._
            intent.putParcelableArrayListExtra( key, new ArrayList( value ) )
        }
    }

    implicit val writerIntentLong: Writer.Intent[Long] = instance( _.putExtra( _, _ ) )

    implicit def writerIntentParcelable[V <: Parcelable]: Writer.Intent[V] = instance( _.putExtra( _, _ ) )

    def writerIntentSerializable[V <: Serializable]: Writer.Intent[V] = instance( _.putExtra( _, _ ) )

    implicit val writerIntentShort: Writer.Intent[Short] = instance( _.putExtra( _, _ ) )

    implicit val writerIntentString: Writer.Intent[String] = instance( _.putExtra( _, _ ) )

    implicit def writerEnumeration[C, V: Enum.Derived]( implicit w: Writer[C, String] ): Writer[C, V] = {
        w.contramap( Enum[V].encode )
    }

    implicit def writerOption[C, V]( implicit w: Writer[C, V] ): Writer[C, Option[V]] = {
        instance( ( container, key, value ) ⇒ value.foreach( w.write( container, key, _ ) ) )
    }

    implicit def writerURL[C]( implicit w: Writer[C, String] ): Writer[C, URL] = w.contramap( _.toString )
}

trait Writer1 extends Writer2 {
    implicit def writerArrayIterable[C, V](
        implicit
        w: Writer[C, Iterable[V]]
    ): Writer[C, Array[V]] = w.contramap( _.toIterable )

    implicit def writerIterableArray[C, V: ClassTag, F[α] <: Iterable[α]](
        implicit
        w: Writer[C, Array[V]]
    ): Writer[C, F[V]] = w.contramap( _.toArray )
}

trait Writer2 extends WriterOperations {
    implicit def writerEncoder[C, V]( implicit w: Lazy[Writer[C, Bundle]], e: Lazy[Encoder[V]] ): Writer[C, V] = {
        instance( ( container, key, value ) ⇒ w.value.write( container, key, e.value.encode( value ) ) )
    }
}

trait WriterOperations {
    implicit def contravariantWriter[C]: Contravariant[Writer[C, ?]] = new Contravariant[Writer[C, ?]] {
        override def contramap[A, B]( fa: Writer[C, A] )( f: B ⇒ A ): Writer[C, B] = {
            instance( ( container, key, value ) ⇒ fa.write( container, key, f( value ) ) )
        }
    }

    def apply[C, V]( implicit w: Writer[C, V] ): Writer[C, V] = w

    def instance[C, V]( f: ( C, String, V ) ⇒ Unit ): Writer[C, V] = new Writer[C, V] {
        override def write( container: C, key: String, value: V ): Unit = f( container, key, value )
    }
}