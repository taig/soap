package io.taig.android.soap

import cats.functor.Contravariant
import io.taig.android.soap.syntax.writer._
import shapeless._
import shapeless.syntax.singleton._
import shapeless.labelled.FieldType

import scala.language.higherKinds
import scala.reflect.{ ClassTag, classTag }

/**
 * Type class which describes how to convert a value V to a Bundle
 */
trait Encoder[V] {
    def encode( value: V ): Bundle
}

object Encoder extends Encoder0

trait Encoder0 extends Encoder1 {
    implicit val encoderCNil: Encoder[CNil] = instance(
        _ ⇒ sys.error( "No Encoder representation for CNil (this shouldn't happen)" )
    )

    implicit val encoderHNil: Encoder[HNil] = instance( _ ⇒ Bundle.empty )

    implicit def encoderCoproduct[H: ClassTag, T <: Coproduct](
        implicit
        h: Lazy[Encoder[H]],
        t: Lazy[Encoder[T]]
    ): Encoder[H :+: T] = instance {
        case Inl( head ) ⇒ h.value.encode( head ).write( "type", classTag[H].runtimeClass.getCanonicalName )
        case Inr( tail ) ⇒ t.value.encode( tail )
    }

    implicit def encoderRecord[K <: Symbol, V, T <: HList](
        implicit
        lf: fold.F[FieldType[K, V] :: T, Bundle]
    ): Encoder[FieldType[K, V] :: T] = instance { list ⇒ list.foldLeft( Bundle( list.runtimeLength ) )( fold ) }

    implicit def encoderArrayWriter[V](
        implicit
        w: Lazy[Writer[Bundle, V]]
    ): Encoder[Array[V]] = instance( array ⇒ encodeCollection[V]( array.toSeq )( w.value ) )

    implicit def encoderIterableWriter[V, F[+α] <: Iterable[α]](
        implicit
        w: Lazy[Writer[Bundle, V]]
    ): Encoder[F[V]] = instance( encodeCollection[V]( _ )( w.value ) )

    implicit def encoderMapStringWriter[V, M[α, +β] <: Map[α, β]](
        implicit
        w: Lazy[Writer[Bundle, V]]
    ): Encoder[M[String, V]] = instance { values ⇒
        val bundle = Bundle( values.size )
        values.foreach { case ( key, value ) ⇒ bundle.write( key, value )( w.value ) }
        bundle
    }

    implicit def encoderMapEncoderEncoder[K, V, M[α, +β] <: Map[α, β]](
        implicit
        ek: Lazy[Encoder[Iterable[K]]],
        ev: Lazy[Encoder[Iterable[V]]]
    ): Encoder[M[K, V]] = instance { values ⇒
        Bundle( 2 ).write(
            "keys" ->> ek.value.encode( values.keys ) ::
                "values" ->> ev.value.encode( values.values ) ::
                HNil
        )
    }

    private def encodeCollection[V]( values: Iterable[V] )( implicit w: Writer[Bundle, V] ): Bundle = {
        val bundle = Bundle( values.size )
        values.zipWithIndex.foreach { case ( value, index ) ⇒ bundle.write( index.toString, value ) }
        bundle
    }
}

trait Encoder1 extends Encoder2 {
    implicit def encoderCaseClass[T, LG](
        implicit
        lg: LabelledGeneric.Aux[T, LG],
        e:  Lazy[Encoder[LG]]
    ): Encoder[T] = instance { value ⇒ e.value.encode( lg.to( value ) ) }
}

trait Encoder2 extends EncoderOperations {
    implicit def encoderGeneric[T, G](
        implicit
        g: Generic.Aux[T, G],
        e: Lazy[Encoder[G]]
    ): Encoder[T] = instance { value ⇒ e.value.encode( g.to( value ) ) }
}

trait EncoderOperations {
    implicit val contravariantEncoder: Contravariant[Encoder] = new Contravariant[Encoder] {
        override def contramap[A, B]( fa: Encoder[A] )( f: B ⇒ A ): Encoder[B] = {
            instance( value ⇒ fa.encode( f( value ) ) )
        }
    }

    def apply[V]( implicit e: Encoder[V] ): Encoder[V] = e

    def instance[V]( f: V ⇒ Bundle ): Encoder[V] = new Encoder[V] {
        override def encode( value: V ): Bundle = f( value )
    }
}