package io.taig.android.parcelable.bundler

import cats.Functor
import export.imports
import io.taig.android.parcelable
import io.taig.android.parcelable._
import shapeless._
import shapeless.labelled._
import cats.syntax.functor._

import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import scala.reflect.ClassTag

trait Decoder[V] extends Codec[V] with parcelable.Decoder

object Decoder extends DecoderOperations with Decoders0

@imports[Decoder]
trait Decoders0 extends DecoderOperations with Decoders1 {
    implicit val `Decoder[CNil]`: Decoder[CNil] = Decoder{ _ ⇒
        sys.error( "No Read representation of CNil (this shouldn't happen)" )
    }

    implicit def `Decoder[Coproduct]`[K <: Symbol, H, T <: Coproduct](
        implicit
        k: Witness.Aux[K],
        h: Lazy[bundle.Decoder[H]],
        t: Lazy[Decoder[T]]
    ): Decoder[FieldType[K, H] :+: T] = Decoder { bundle ⇒
        bundle.containsKey( k.value.name ) match {
            case true  ⇒ Inl( field( bundle.read[H]( k.value.name ) ( h.value ) ) )
            case false ⇒ Inr( t.value.decode( bundle ) )
        }
    }

    implicit val `Decoder[HNil]`: Decoder[HNil] = Decoder( _ ⇒ HNil )

    implicit def `Decoder[HList]`[K <: Symbol, V, T <: HList](
        implicit
        key: Witness.Aux[K],
        bv:  bundle.Decoder[V],
        bt:  Decoder[T]
    ): Decoder[FieldType[K, V] :: T] = {
        Decoder( bundle ⇒ field[K]( bundle.read[V]( key.value.name ) ) :: bt.decode( bundle ) )
    }
}

trait Decoders1 extends DecoderOperations with Decoders2 {
    implicit def `Decoder[Array[bundle.Decoder]]`[V: bundle.Decoder: ClassTag]: Decoder[Array[V]] = Decoder { bundle ⇒
        val array = new Array[V]( bundle.size() )

        for ( i ← 0 until bundle.size() ) {
            array( i ) = bundle.read[V]( i.toString )
        }

        array
    }

    implicit def `Decoder[Traversable[bundle.Decoder]]`[V: bundle.Decoder: ClassTag, T[V] <: Traversable[V]](
        implicit
        cbf: CanBuildFrom[Nothing, V, T[V]]
    ): Decoder[T[V]] = `Decoder[Array[bundle.Decoder]]`[V].map( _.to[T] )
}

trait Decoders2 extends DecoderOperations {
    implicit def `Decoder[LabelledGeneric]`[T, LG](
        implicit
        lg: LabelledGeneric.Aux[T, LG],
        b:  Decoder[LG]
    ): Decoder[T] = Decoder( bundle ⇒ lg.from( b.decode( bundle ) ) )
}

trait DecoderOperations {
    def apply[V]( f: Bundle ⇒ V ): Decoder[V] = new Decoder[V] {
        override def decode( bundle: Bundle ) = f( bundle )
    }

    implicit val `Functor[Bundle]`: Functor[Decoder] = new Functor[Decoder] {
        override def map[A, B]( b: Decoder[A] )( f: A ⇒ B ) = new Decoder[B] {
            override def decode( serialization: Bundle ) = f( b.decode( serialization ) )
        }
    }
}