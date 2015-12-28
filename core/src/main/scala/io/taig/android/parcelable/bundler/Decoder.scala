package io.taig.android.parcelable.bundler

import io.taig.android.parcelable
import io.taig.android.parcelable._
import io.taig.android.parcelable.functional._
import io.taig.android.parcelable.syntax._
import shapeless._
import shapeless.labelled._

import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import scala.reflect._

trait Decoder[V] extends parcelable.Decoder[Bundle, V]

object Decoder extends DecoderOperations with Decoders0

trait Decoders0 extends DecoderOperations with Decoders1 {
    implicit val `Decoder[CNil]`: Decoder[CNil] = Decoder.instance(
        _ ⇒ sys.error( "No Decoder representation for CNil (this shouldn't happen)" )
    )

    implicit def `Decoder[Coproduct]`[H: ClassTag, T <: Coproduct](
        implicit
        h: Lazy[bundle.Decoder[H]],
        t: Lazy[Decoder[T]]
    ): Decoder[H :+: T] = Decoder.instance { bundle ⇒
        val name = classTag[H].runtimeClass.getCanonicalName

        bundle.containsKey( name ) match {
            case true  ⇒ Inl( bundle.read[H]( name )( h.value ) )
            case false ⇒ Inr( t.value.decode( bundle ) )
        }
    }

    implicit val `Decoder[HNil]`: Decoder[HNil] = Decoder.instance( _ ⇒ HNil )

    implicit def `Decoder[HList]`[K <: Symbol, V, T <: HList, N <: Nat](
        implicit
        k: Witness.Aux[K],
        h: Lazy[bundle.Decoder[V]],
        t: Lazy[Decoder[T]]
    ): Decoder[FieldType[K, V] :: T] = Decoder.instance { bundle ⇒
        field[K]( bundle.read[V]( k.value.name )( h.value ) ) :: t.value.decode( bundle )
    }

    implicit def `Decoder[Map[String, bundle.Decoder]]`[V, M[K, +V] <: Map[K, V]](
        implicit
        d:   Lazy[bundle.Decoder[V]],
        cbf: CanBuildFrom[Nothing, ( String, V ), M[String, V]]
    ): Decoder[M[String, V]] = Decoder.instance { bundle ⇒
        import collection.JavaConversions._

        val builder = cbf.apply()

        bundle.keySet().map { key ⇒
            builder += ( ( key, bundle.read[V]( key )( d.value ) ) )
        }

        builder.result
    }

    implicit def `Decoder[Traversable[(String, bundle.Decoder)]]`[V, M[+V] <: Traversable[V]](
        implicit
        d:   Lazy[bundle.Decoder[V]],
        cbf: CanBuildFrom[Nothing, ( String, V ), M[( String, V )]]
    ): Decoder[M[( String, V )]] = Decoder[Map[String, V]].map( _.to[M] )
}

trait Decoders1 extends DecoderOperations with Decoders2 {
    implicit def `Decoder[Array[bundle.Decoder]]`[V: ClassTag](
        implicit
        d: Lazy[bundle.Decoder[V]]
    ): Decoder[Array[V]] = Decoder.instance { bundle ⇒
        val array = new Array[V]( bundle.size() )

        for ( i ← 0 until bundle.size() ) {
            array( i ) = bundle.read[V]( i.toString )( d.value )
        }

        array
    }

    implicit def `Decoder[Array[Option[bundle.Decoder]]]`[V: ClassTag](
        implicit
        d: Lazy[bundle.Decoder[Option[V]]]
    ): Decoder[Array[Option[V]]] = Decoder.instance { bundle ⇒
        val length = bundle.read[Int]( "length" )
        val array = new Array[Option[V]]( length )

        for ( i ← 0 until length ) {
            array( i ) = bundle.read[Option[V]]( i.toString )( d.value )
        }

        array
    }

    implicit def `Decoder[Traversable[bundle.Decoder]]`[V: ClassTag, T[V] <: Traversable[V]](
        implicit
        d:   Lazy[bundle.Decoder[V]],
        cbf: CanBuildFrom[Nothing, V, T[V]]
    ): Decoder[T[V]] = Decoder[Array[V]].map( _.to[T] )

    implicit def `Decoder[Traversable[Option[bundle.Decoder]]]`[V: ClassTag, T[V] <: Traversable[V]](
        implicit
        d:   Lazy[bundle.Decoder[Option[V]]],
        cbf: CanBuildFrom[Nothing, Option[V], T[Option[V]]]
    ): Decoder[T[Option[V]]] = Decoder[Array[Option[V]]].map( _.to[T] )
}

trait Decoders2 extends DecoderOperations with Decoders3 {
    implicit def `Decoder[LabelledGeneric]`[T, LG](
        implicit
        lg: LabelledGeneric.Aux[T, LG],
        c:  Lazy[Decoder[LG]]
    ): Decoder[T] = Decoder.instance( bundle ⇒ lg.from( c.value.decode( bundle ) ) )
}

trait Decoders3 extends DecoderOperations {
    implicit def `Decoder[Generic]`[T, G](
        implicit
        g: Generic.Aux[T, G],
        c: Lazy[Decoder[G]]
    ): Decoder[T] = Decoder.instance( bundle ⇒ g.from( c.value.decode( bundle ) ) )
}

trait DecoderOperations {
    def apply[V: Decoder]: Decoder[V] = implicitly[Decoder[V]]

    def instance[V]( f: Bundle ⇒ V ): Decoder[V] = new Decoder[V] {
        override def decode( bundle: Bundle ) = f( bundle )
    }

    implicit val `Functor[Decoder]`: Functor[Decoder] = new Functor[Decoder] {
        override def map[A, B]( b: Decoder[A] )( f: A ⇒ B ) = instance( bundle ⇒ f( b.decode( bundle ) ) )
    }
}