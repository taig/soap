package io.taig.android.parcelable.bundler

import cats.functor.Contravariant
import cats.syntax.contravariant._
import export.imports
import io.taig.android.parcelable
import io.taig.android.parcelable._
import shapeless.Nat._
import shapeless._
import shapeless.labelled.FieldType
import shapeless.ops.hlist.{ LeftFolder, Length }
import shapeless.ops.nat.ToInt

import scala.language.higherKinds
import scala.reflect.ClassTag

trait Encoder[V] extends Codec[V] with parcelable.Encoder

object Encoder extends EncoderOperations with Encoders0

@imports[Encoder]
trait Encoders0 extends EncoderOperations with Encoders1 {
    implicit val `Encoder[CNil]`: Encoder[CNil] = Encoder { _ ⇒
        sys.error( "No Write representation of CNil (this shouldn't happen)" )
    }

    implicit def `Encoder[Coproduct]`[K <: Symbol, H, T <: Coproduct](
        implicit
        k: Witness.Aux[K],
        h: bundle.Encoder[H],
        t: Encoder[T]
    ): Encoder[FieldType[K, H] :+: T] = Encoder {
        case Inl( head ) ⇒ Bundle[H]( k.value.name, head )( h )
        case Inr( tail ) ⇒ t.encode( tail )
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
    implicit def `Encoder[Array[bundle.Encoder]]`[V: bundle.Encoder]: Encoder[Array[V]] = Encoder { values ⇒
        val bundle = Bundle( values.length )

        for ( i ← values.indices ) {
            bundle.write( i.toString, values( i ) )
        }

        bundle
    }

    implicit def `Encoder[Traversable[bundle.Encoder]]`[V: bundle.Encoder: ClassTag, T[V] <: Traversable[V]]: Encoder[T[V]] = {
        `Encoder[Array[bundle.Encoder]]`[V].contramap( _.toArray )
    }
}

trait Encoders2 extends EncoderOperations {
    implicit def `Encoder[LabelledGeneric]`[T, LG](
        implicit
        lg: LabelledGeneric.Aux[T, LG],
        e:  Encoder[LG]
    ): Encoder[T] = Encoder( value ⇒ e.encode( lg.to( value ) ) )
}

trait EncoderOperations {
    def apply[V]( f: V ⇒ Bundle ): Encoder[V] = new Encoder[V] {
        override def encode( value: V ) = f( value )
    }

    implicit val `Contravariant[Bundle]`: Contravariant[Encoder] = new Contravariant[Encoder] {
        override def contramap[A, B]( b: Encoder[A] )( f: B ⇒ A ) = new Encoder[B] {
            override def encode( value: B ) = b.encode( f( value ) )
        }
    }
}