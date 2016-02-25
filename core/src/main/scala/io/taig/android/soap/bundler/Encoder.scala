package io.taig.android.soap.bundler

import io.taig.android.soap
import io.taig.android.soap._
import io.taig.android.soap.functional._
import io.taig.android.soap.syntax.bundle._
import io.taig.android.soap.syntax.functional._
import shapeless.Nat._
import shapeless._
import shapeless.labelled._
import shapeless.ops.hlist.{ LeftFolder, Length }
import shapeless.ops.nat.ToInt

import scala.language.higherKinds
import scala.reflect._

trait Encoder[V] extends soap.Encoder[V, Bundle]

object Encoder extends EncoderOperations with Encoders0

trait Encoders0 extends EncoderOperations with Encoders1 {
    implicit val encoderCNil: Encoder[CNil] = Encoder.instance(
        _ ⇒ sys.error( "No Encoder representation for CNil (this shouldn't happen)" )
    )

    implicit def encoderCoproduct[H: ClassTag, T <: Coproduct](
        implicit
        h: Lazy[bundle.Encoder[H]],
        t: Lazy[Encoder[T]]
    ): Encoder[H :+: T] = Encoder.instance {
        case Inl( head ) ⇒ Bundle[H]( classTag[H].runtimeClass.getCanonicalName, head )( h.value )
        case Inr( tail ) ⇒ t.value.encode( tail )
    }

    implicit val encoderHNil: Encoder[HNil] = Encoder.instance( _ ⇒ Bundle.empty )

    implicit def encoderHList[K <: Symbol, V, T <: HList, N <: Nat](
        implicit
        l:  Length.Aux[FieldType[K, V] :: T, N],
        ti: ToInt[N],
        lf: LeftFolder.Aux[FieldType[K, V] :: T, Bundle, fold.type, Bundle]
    ): Encoder[FieldType[K, V] :: T] = Encoder.instance( _.foldLeft( Bundle( toInt[N] ) )( fold ) )

    implicit def encoderMapStringbundleEncoder[V, M[K, +V] <: Map[K, V]](
        implicit
        e: Lazy[bundle.Encoder[V]]
    ): Encoder[M[String, V]] = Encoder.instance { values ⇒
        values.foldLeft( Bundle( values.size ) ) {
            case ( bundle, ( key, value ) ) ⇒ bundle.write( key, value )( e.value )
        }
    }

    implicit def encoderTraversableStringBundleEncoder[V, M[+V] <: Traversable[V]](
        implicit
        e: Lazy[bundle.Encoder[V]]
    ): Encoder[M[( String, V )]] = Encoder[Map[String, V]].contramap( _.toMap )
}

trait Encoders1 extends EncoderOperations with Encoders2 {
    implicit def encoderArrayBundleEncoder[V](
        implicit
        e: Lazy[bundle.Encoder[V]]
    ): Encoder[Array[V]] = Encoder.instance { values ⇒
        val bundle = Bundle( values.length )

        for ( i ← values.indices ) {
            bundle.write( i.toString, values( i ) )( e.value )
        }

        bundle
    }

    implicit def encoderArrayOptionBundleEncoder[V](
        implicit
        e: Lazy[bundle.Encoder[Option[V]]]
    ): Encoder[Array[Option[V]]] = Encoder.instance { values ⇒
        val length = values.length
        val bundle = Bundle( values.count( _.isDefined ) )

        bundle.write( "length", length )

        for ( i ← values.indices ) {
            bundle.write( i.toString, values( i ) )( e.value )
        }

        bundle
    }

    implicit def encoderTraversableBundleEncoder[V: ClassTag, T[V] <: Traversable[V]](
        implicit
        c: Lazy[bundle.Encoder[V]]
    ): Encoder[T[V]] = Encoder[Array[V]].contramap( _.toArray )

    implicit def encoderTraversableOptionBundleEncoder[V: ClassTag, T[V] <: Traversable[V]](
        implicit
        e: Lazy[bundle.Encoder[Option[V]]]
    ): Encoder[T[Option[V]]] = Encoder[Array[Option[V]]].contramap( _.toArray )
}

trait Encoders2 extends EncoderOperations with Encoders3 {
    implicit def encoderLabelledGeneric[T, LG](
        implicit
        lg: LabelledGeneric.Aux[T, LG],
        e:  Lazy[Encoder[LG]]
    ): Encoder[T] = Encoder.instance( value ⇒ e.value.encode( lg.to( value ) ) )
}

trait Encoders3 extends EncoderOperations {
    implicit def encoderGeneric[T, G](
        implicit
        g: Generic.Aux[T, G],
        e: Lazy[Encoder[G]]
    ): Encoder[T] = Encoder.instance( value ⇒ e.value.encode( g.to( value ) ) )
}

trait EncoderOperations {
    def apply[V: Encoder]: Encoder[V] = implicitly[Encoder[V]]

    def instance[V]( f: V ⇒ Bundle ): Encoder[V] = new Encoder[V] {
        override def encode( value: V ) = f( value )
    }

    implicit val contravariantEncoder: Contravariant[Encoder] = new Contravariant[Encoder] {
        override def contramap[A, B]( b: Encoder[A] )( f: B ⇒ A ) = instance( value ⇒ b.encode( f( value ) ) )
    }
}