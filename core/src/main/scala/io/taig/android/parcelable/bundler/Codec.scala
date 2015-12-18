package io.taig.android.parcelable.bundler

import io.taig.android.parcelable
import io.taig.android.parcelable._
import io.taig.android.parcelable.functional.{ Contravariant, Functor, Inmap }
import io.taig.android.parcelable.syntax._
import shapeless.Nat._
import shapeless._
import shapeless.labelled._
import shapeless.ops.hlist.{ LeftFolder, Length }
import shapeless.ops.nat.ToInt

import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import scala.reflect.ClassTag

trait Codec[V]
    extends parcelable.Codec.Symmetric[Bundle, V]
    with Encoder[V]
    with Decoder[V]

object Codec extends CodecOperations with Codecs0

trait Codecs0 extends CodecOperations with Codecs1 {
    implicit val `Codec[CNil]`: Codec[CNil] = Codec.instance(
        _ ⇒ sys.error( "No Codec representation for CNil (this shouldn't happen)" ),
        _ ⇒ sys.error( "No Codec representation for CNil (this shouldn't happen)" )
    )

    implicit def `Codec[Coproduct]`[K <: Symbol, H, T <: Coproduct](
        implicit
        k: Witness.Aux[K],
        h: Lazy[bundle.Codec[H]],
        t: Lazy[Codec[T]]
    ): Codec[FieldType[K, H] :+: T] = Codec.instance(
        {
            case Inl( head ) ⇒ Bundle[H]( k.value.name, head )( h.value )
            case Inr( tail ) ⇒ t.value.encode( tail )
        },
        bundle ⇒ bundle.containsKey( k.value.name ) match {
            case true  ⇒ Inl( field( bundle.read[H]( k.value.name ) ( h.value ) ) )
            case false ⇒ Inr( t.value.decode( bundle ) )
        }
    )

    implicit val `Codec[HNil]`: Codec[HNil] = Codec.instance(
        _ ⇒ Bundle.empty,
        _ ⇒ HNil
    )

    implicit def `Codec[HList]`[K <: Symbol, V, T <: HList, N <: Nat](
        implicit
        k:  Witness.Aux[K],
        l:  Length.Aux[FieldType[K, V] :: T, N],
        ti: ToInt[N],
        lf: LeftFolder.Aux[FieldType[K, V] :: T, Bundle, fold.type, Bundle],
        h:  Lazy[bundle.Codec[V]],
        t:  Lazy[Codec[T]]
    ): Codec[FieldType[K, V] :: T] = Codec.instance(
        _.foldLeft( Bundle( toInt[N] ) )( fold ),
        bundle ⇒ field[K]( bundle.read[V]( k.value.name )( h.value ) ) :: t.value.decode( bundle )
    )

    implicit def `Codec[Map[String, bundle.Codec]]`[V, M[K, +V] <: Map[K, V]](
        implicit
        c:   Lazy[bundle.Codec[V]],
        cbf: CanBuildFrom[Nothing, ( String, V ), M[String, V]]
    ): Codec[M[String, V]] = Codec.instance(
        values ⇒ values.foldLeft( Bundle( values.size ) ) {
            case ( bundle, ( key, value ) ) ⇒ bundle.write( key, value )( c.value )
        },
        bundle ⇒ {
            import collection.JavaConversions._

            val builder = cbf.apply()

            bundle.keySet().map { key ⇒
                builder += ( ( key, bundle.read[V]( key )( c.value ) ) )
            }

            builder.result
        }
    )

    implicit def `Codec[Traversable[(String, bundle.Codec)]]`[V, M[+V] <: Traversable[V]](
        implicit
        c:   Lazy[bundle.Codec[V]],
        cbf: CanBuildFrom[Nothing, ( String, V ), M[( String, V )]]
    ): Codec[M[( String, V )]] = Codec[Map[String, V]].inmap( _.toMap, _.to[M] )
}

trait Codecs1 extends CodecOperations with Codecs2 {
    implicit def `Codec[Array[bundle.Codec]]`[V: ClassTag](
        implicit
        c: Lazy[bundle.Codec[V]]
    ): Codec[Array[V]] = Codec.instance(
        values ⇒ {
            val bundle = Bundle( values.length )

            for ( i ← values.indices ) {
                bundle.write( i.toString, values( i ) )( c.value )
            }

            bundle
        },
        bundle ⇒ {
            val array = new Array[V]( bundle.size() )

            for ( i ← 0 until bundle.size() ) {
                array( i ) = bundle.read[V]( i.toString )( c.value )
            }

            array
        }
    )

    implicit def `Codec[Array[Option[bundle.Codec]]]`[V](
        implicit
        c: Lazy[bundle.Codec[Option[V]]]
    ): Codec[Array[Option[V]]] = Codec.instance(
        values ⇒ {
            val length = values.length
            val bundle = Bundle( values.count( _.isDefined ) )

            bundle.write( "length", length )

            for ( i ← values.indices ) {
                bundle.write( i.toString, values( i ) )( c.value )
            }

            bundle
        },
        bundle ⇒ {
            val length = bundle.read[Int]( "length" )
            val array = new Array[Option[V]]( length )

            for ( i ← 0 until length ) {
                array( i ) = bundle.read[Option[V]]( i.toString )( c.value )
            }

            array
        }
    )

    implicit def `Codec[Traversable[bundle.Codec]]`[V: ClassTag, T[V] <: Traversable[V]](
        implicit
        c:   Lazy[bundle.Codec[V]],
        cbf: CanBuildFrom[Nothing, V, T[V]]
    ): Codec[T[V]] = Codec[Array[V]].inmap( _.toArray, _.to[T] )

    implicit def `Codec[Traversable[Option[bundle.Codec]]]`[V: ClassTag, T[V] <: Traversable[V]](
        implicit
        c:   Lazy[bundle.Codec[Option[V]]],
        cbf: CanBuildFrom[Nothing, Option[V], T[Option[V]]]
    ): Codec[T[Option[V]]] = Codec[Array[Option[V]]].inmap( _.toArray, _.to[T] )
}

trait Codecs2 extends CodecOperations {
    implicit def `Codec[LabelledGeneric]`[T, LG](
        implicit
        lg: LabelledGeneric.Aux[T, LG],
        c:  Lazy[Codec[LG]]
    ): Codec[T] = Codec.instance(
        value ⇒ c.value.encode( lg.to( value ) ),
        bundle ⇒ lg.from( c.value.decode( bundle ) )
    )
}

trait CodecOperations {
    def apply[V: Codec]: Codec[V] = implicitly[Codec[V]]

    def instance[V]( e: V ⇒ Bundle, d: Bundle ⇒ V ): Codec[V] = new Codec[V] {
        override def encode( value: V ) = e( value )

        override def decode( serialization: Bundle ) = d( serialization )
    }

    implicit val `Inmap[Codec]`: Inmap[Codec] = new Inmap[Codec] {
        override def inmap[A, B]( fa: Codec[A] )( contramap: B ⇒ A, map: A ⇒ B ) = instance(
            { case value ⇒ implicitly[Contravariant[Encoder]].contramap( fa )( contramap ).encode( value ) },
            { case serialization ⇒ implicitly[Functor[Decoder]].map( fa )( map ).decode( serialization ) }
        )
    }
}