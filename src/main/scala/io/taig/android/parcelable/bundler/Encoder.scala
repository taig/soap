package io.taig.android.parcelable.bundler

import io.taig.android.parcelable
import io.taig.android.parcelable._
import io.taig.android.parcelable.functional._
import io.taig.android.parcelable.syntax._
import shapeless.Nat._
import shapeless._
import shapeless.labelled.FieldType
import shapeless.ops.hlist.{ LeftFolder, Length }
import shapeless.ops.nat.ToInt

import scala.language.higherKinds
import scala.reflect.ClassTag

trait Encoder[V] extends Codec[V] with parcelable.Encoder

object Encoder extends EncoderOperations with Encoders0

trait Encoders0 extends EncoderOperations with Encoders1 {
    implicit val `Encoder[CNil]`: Encoder[CNil] = Encoder { _ ⇒
        sys.error( "No Encoder representation of CNil (this shouldn't happen)" )
    }

    implicit def `Encoder[Coproduct]`[K <: Symbol, H, T <: Coproduct](
        implicit
        k: Witness.Aux[K],
        h: Lazy[bundle.Encoder[H]],
        t: Lazy[Encoder[T]]
    ): Encoder[FieldType[K, H] :+: T] = Encoder {
        case Inl( head ) ⇒ Bundle[H]( k.value.name, head )( h.value )
        case Inr( tail ) ⇒ t.value.encode( tail )
    }

    implicit val `Encoder[HNil]`: Encoder[HNil] = Encoder( _ ⇒ Bundle.empty )

    implicit def `Encoder[HList]`[K <: Symbol, V, T <: HList, N <: Nat](
        implicit
        l:  Length.Aux[FieldType[K, V] :: T, N],
        ti: ToInt[N],
        lf: LeftFolder.Aux[FieldType[K, V] :: T, Bundle, fold.type, Bundle]
    ): Encoder[FieldType[K, V] :: T] = Encoder( _.foldLeft( Bundle( toInt[N] ) )( fold ) )
}

trait Encoders1 extends EncoderOperations with Encoders2 {
    implicit def `Encoder[Array[bundle.Encoder]]`[V](
        implicit
        e: Lazy[bundle.Encoder[V]]
    ): Encoder[Array[V]] = Encoder { values ⇒
        val bundle = Bundle( values.length )

        for ( i ← values.indices ) {
            bundle.write( i.toString, values( i ) )( e.value )
        }

        bundle
    }

    implicit def `Encoder[Array[Option[bundle.Encoder]]]`[V](
        implicit
        e: Lazy[bundle.Encoder[Option[V]]]
    ): Encoder[Array[Option[V]]] = Encoder { values ⇒
        val length = values.length
        val bundle = Bundle( values.count( _.isDefined ) )

        bundle.write( "length", length )

        for ( i ← values.indices ) {
            bundle.write( i.toString, values( i ) )( e.value )
        }

        bundle
    }

    implicit def `Encoder[Traversable[bundle.Encoder]]`[V: ClassTag, T[V] <: Traversable[V]](
        implicit
        e: Lazy[bundle.Encoder[V]]
    ): Encoder[T[V]] = {
        `Encoder[Array[bundle.Encoder]]`[V].contramap( _.toArray )
    }

    implicit def `Encoder[Traversable[Option[bundle.Encoder]]]`[V: ClassTag, T[V] <: Traversable[V]](
        implicit
        e: Lazy[bundle.Encoder[Option[V]]]
    ): Encoder[T[Option[V]]] = {
        `Encoder[Array[Option[bundle.Encoder]]]`[V].contramap( _.toArray )
    }
}

trait Encoders2 extends EncoderOperations {
    implicit def `Encoder[LabelledGeneric]`[T, LG](
        implicit
        lg: LabelledGeneric.Aux[T, LG],
        e:  Lazy[Encoder[LG]]
    ): Encoder[T] = Encoder( value ⇒ e.value.encode( lg.to( value ) ) )
}

trait EncoderOperations {
    def apply[V]( f: V ⇒ Bundle ): Encoder[V] = new Encoder[V] {
        override def encode( value: V ) = f( value )
    }

    implicit val `Contramap[Bundle]`: Contramap[Encoder] = new Contramap[Encoder] {
        override def contramap[A, B]( b: Encoder[A] )( f: B ⇒ A ) = new Encoder[B] {
            override def encode( value: B ) = b.encode( f( value ) )
        }
    }
}