package io.taig.android.soap

import java.net.URL

import android.content.{ Intent ⇒ AIntent }
import android.os.{ Parcelable, Bundle ⇒ ABundle }
import cats.Functor
import cats.syntax.functor._
import julienrf.enum.Enum
import shapeless.Lazy

import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import scala.reflect.ClassTag

/**
 * Type class which describes how to read a value V from a container C
 */
trait Reader[C, V] {
    def read( container: C, key: String ): Option[V]
}

object Reader extends Reader0 {
    type Bundle[V] = Reader[ABundle, V]

    type Intent[V] = Reader[AIntent, V]
}

trait Reader0 extends Reader1 {
    implicit val readerBundleArrayBoolean: Reader.Bundle[Array[Boolean]] = instanceNullable( _.getBooleanArray( _ ) )

    implicit val readerBundleArrayByte: Reader.Bundle[Array[Byte]] = instanceNullable( _.getByteArray( _ ) )

    implicit val readerBundleArrayChar: Reader.Bundle[Array[Char]] = instanceNullable( _.getCharArray( _ ) )

    implicit val readerBundleArrayDouble: Reader.Bundle[Array[Double]] = instanceNullable( _.getDoubleArray( _ ) )

    implicit val readerBundleArrayFloat: Reader.Bundle[Array[Float]] = instanceNullable( _.getFloatArray( _ ) )

    implicit val readerBundleArrayInt: Reader.Bundle[Array[Int]] = instanceNullable( _.getIntArray( _ ) )

    implicit val readerBundleArrayLong: Reader.Bundle[Array[Long]] = instanceNullable( _.getLongArray( _ ) )

    implicit val readerBundleArrayShort: Reader.Bundle[Array[Short]] = instanceNullable( _.getShortArray( _ ) )

    implicit val readerBundleArrayString: Reader.Bundle[Array[String]] = instanceNullable( _.getStringArray( _ ) )

    implicit val readerBundleBoolean: Reader.Bundle[Boolean] = instanceGuardedBundle( _.getBoolean( _ ) )

    implicit val readerBundleBundle: Reader.Bundle[Bundle] = instanceNullable( _.getBundle( _ ) )

    implicit val readerBundleByte: Reader.Bundle[Byte] = instanceGuardedBundle( _.getByte( _ ) )

    implicit val readerBundleChar: Reader.Bundle[Char] = instanceGuardedBundle( _.getChar( _ ) )

    implicit val readerBundleCharSequence: Reader.Bundle[CharSequence] = instanceNullable( _.getCharSequence( _ ) )

    implicit val readerBundleDouble: Reader.Bundle[Double] = instanceGuardedBundle( _.getDouble( _ ) )

    implicit val readerBundleFloat: Reader.Bundle[Float] = instanceGuardedBundle( _.getFloat( _ ) )

    implicit val readerBundleInt: Reader.Bundle[Int] = instanceGuardedBundle( _.getInt( _ ) )

    implicit def readerBundleIterableParcelable[V <: Parcelable, F[α] <: Iterable[α]](
        implicit
        cbf: CanBuildFrom[Nothing, V, F[V]]
    ): Reader.Bundle[F[V]] = {
        import scala.collection.JavaConversions._
        instance( ( bundle, key ) ⇒ Option( bundle.getParcelableArrayList[V]( key ) ).map( _.to[F] ) )
    }

    implicit val readerBundleLong: Reader.Bundle[Long] = instanceGuardedBundle( _.getLong( _ ) )

    implicit def readerBundleParcelable[V <: Parcelable]: Reader.Bundle[V] = instanceNullable( _.getParcelable[V]( _ ) )

    implicit val readerBundleShort: Reader.Bundle[Short] = instanceGuardedBundle( _.getShort( _ ) )

    implicit val readerBundleString: Reader.Bundle[String] = instanceNullable( _.getString( _ ) )

    implicit val readerIntentArrayBoolean: Reader.Intent[Array[Boolean]] = instanceNullable( _.getBooleanArrayExtra( _ ) )

    implicit val readerIntentArrayByte: Reader.Intent[Array[Byte]] = instanceNullable( _.getByteArrayExtra( _ ) )

    implicit val readerIntentArrayChar: Reader.Intent[Array[Char]] = instanceNullable( _.getCharArrayExtra( _ ) )

    implicit val readerIntentArrayDouble: Reader.Intent[Array[Double]] = instanceNullable( _.getDoubleArrayExtra( _ ) )

    implicit val readerIntentArrayFloat: Reader.Intent[Array[Float]] = instanceNullable( _.getFloatArrayExtra( _ ) )

    implicit val readerIntentArrayInt: Reader.Intent[Array[Int]] = instanceNullable( _.getIntArrayExtra( _ ) )

    implicit val readerIntentArrayLong: Reader.Intent[Array[Long]] = instanceNullable( _.getLongArrayExtra( _ ) )

    implicit val readerIntentArrayShort: Reader.Intent[Array[Short]] = instanceNullable( _.getShortArrayExtra( _ ) )

    implicit val readerIntentArrayString: Reader.Intent[Array[String]] = instanceNullable( _.getStringArrayExtra( _ ) )

    implicit val readerIntentBoolean: Reader.Intent[Boolean] = instanceGuardedIntent( _.getBooleanExtra( _, false ) )

    implicit val readerIntentBundle: Reader.Intent[Bundle] = instanceNullable( _.getBundleExtra( _ ) )

    implicit val readerIntentByte: Reader.Intent[Byte] = instanceGuardedIntent( _.getByteExtra( _, Byte.MinValue ) )

    implicit val readerIntentChar: Reader.Intent[Char] = instanceGuardedIntent( _.getCharExtra( _, Char.MinValue ) )

    implicit val readerIntentCharSequence: Reader.Intent[CharSequence] = instanceNullable( _.getCharSequenceExtra( _ ) )

    implicit val readerIntentDouble: Reader.Intent[Double] = instanceGuardedIntent( _.getDoubleExtra( _, Double.MinValue ) )

    implicit val readerIntentFloat: Reader.Intent[Float] = instanceGuardedIntent( _.getFloatExtra( _, Float.MinValue ) )

    implicit val readerIntentInt: Reader.Intent[Int] = instanceGuardedIntent( _.getIntExtra( _, Int.MinValue ) )

    implicit def readerIntentIterableParcelable[V <: Parcelable, F[α] <: Iterable[α]](
        implicit
        cbf: CanBuildFrom[Nothing, V, F[V]]
    ): Reader.Intent[F[V]] = {
        import scala.collection.JavaConversions._
        instance( ( intent, key ) ⇒ Option( intent.getParcelableArrayListExtra[V]( key ) ).map( _.to[F] ) )
    }

    implicit val readerIntentLong: Reader.Intent[Long] = instanceGuardedIntent( _.getLongExtra( _, Long.MinValue ) )

    implicit def readerIntentParcelable[V <: Parcelable]: Reader.Intent[V] = instanceNullable( _.getParcelableExtra[V]( _ ) )

    implicit val readerIntentShort: Reader.Intent[Short] = instanceGuardedIntent( _.getShortExtra( _, Short.MinValue ) )

    implicit val readerIntentString: Reader.Intent[String] = instanceNullable( _.getStringExtra( _ ) )

    implicit def readerEnumeration[C, V: Enum.Derived]( implicit r: Reader[C, String] ): Reader[C, V] = {
        instance( r.read( _, _ ).flatMap( Enum[V].decodeOpt ) )
    }

    implicit def readerOption[C, V]( implicit r: Reader[C, V] ): Reader[C, Option[V]] = {
        instance( ( container, key ) ⇒ Some( r.read( container, key ) ) )
    }

    implicit def readerURL[C]( implicit r: Reader[C, String] ): Reader[C, URL] = r.map( new URL( _ ) )
}

trait Reader1 extends Reader2 {
    implicit def readerArrayIterable[C, V: ClassTag](
        implicit
        r: Reader[C, Iterable[V]]
    ): Reader[C, Array[V]] = r.map( _.toArray )

    implicit def readerIterableArray[C, V, F[α] <: Iterable[α]](
        implicit
        r:   Reader[C, Array[V]],
        cbf: CanBuildFrom[Nothing, V, F[V]]
    ): Reader[C, F[V]] = r.map( _.to[F] )
}

trait Reader2 extends ReaderOperations {
    implicit def readerDecoder[C, V]( implicit r: Lazy[Reader[C, Bundle]], d: Lazy[Decoder[V]] ): Reader[C, V] = {
        instance( ( container, key ) ⇒ r.value.read( container, key ).flatMap( d.value.decode ) )
    }
}

trait ReaderOperations {
    implicit def functorReader[C]: Functor[Reader[C, ?]] = new Functor[Reader[C, ?]] {
        override def map[A, B]( fa: Reader[C, A] )( f: A ⇒ B ): Reader[C, B] = {
            instance( ( container, key ) ⇒ fa.read( container, key ).map( f ) )
        }
    }

    def apply[C, V]( implicit r: Reader[C, V] ): Reader[C, V] = r

    def instance[C, V]( f: ( C, String ) ⇒ Option[V] ): Reader[C, V] = new Reader[C, V] {
        override def read( container: C, key: String ): Option[V] = f( container, key )
    }

    def instanceNullable[C, V]( f: ( C, String ) ⇒ V ): Reader[C, V] = {
        instance( ( container, key ) ⇒ Option( f( container, key ) ) )
    }

    def instanceGuarded[C, V]( f: ( C, String ) ⇒ Boolean, g: ( C, String ) ⇒ V ): Reader[C, V] = instance {
        case ( container, key ) if f( container, key ) ⇒ Option( g( container, key ) )
        case _                                         ⇒ None
    }

    private[soap] def instanceGuardedBundle[V]( f: ( ABundle, String ) ⇒ V ): Reader.Bundle[V] = {
        instanceGuarded( _.containsKey( _ ), f )
    }

    private[soap] def instanceGuardedIntent[V]( f: ( AIntent, String ) ⇒ V ): Reader.Intent[V] = {
        instanceGuarded( _.hasExtra( _ ), f )
    }
}