package io.taig.android.soap

import cats.Functor
import io.taig.android.soap.syntax.reader._
import shapeless._
import shapeless.labelled._

import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import scala.reflect.{ ClassTag, classTag }

/**
 * Type class which describes how to convert a Bundle to a value V
 */
trait Decoder[V] {
    def decode( bundle: Bundle ): Option[V]
}

object Decoder extends Decoder0

trait Decoder0 extends Decoder1 {
    implicit val decoderCNil: Decoder[CNil] = instance(
        _ ⇒ sys.error( "No Decoder representation for CNil (this shouldn't happen)" )
    )

    implicit val decoderHNil: Decoder[HNil] = instance( _ ⇒ Some( HNil ) )

    implicit def decoderCoproduct[H: ClassTag, T <: Coproduct](
        implicit
        h: Lazy[Decoder[H]],
        t: Lazy[Decoder[T]]
    ): Decoder[H :+: T] = instance { bundle ⇒
        val name = classTag[H].runtimeClass.getCanonicalName

        bundle.read[String]( "type" ).flatMap {
            case `name` ⇒ h.value.decode( bundle ).map( Inl.apply )
            case _      ⇒ t.value.decode( bundle ).map( Inr.apply )
        }
    }

    implicit def decoderRecord[K <: Symbol, V, T <: HList, N <: Nat](
        implicit
        k: Witness.Aux[K],
        h: Lazy[Reader.Bundle[V]],
        t: Lazy[Decoder[T]]
    ): Decoder[FieldType[K, V] :: T] = instance { bundle ⇒
        for {
            h ← bundle.read[V]( k.value.name )( h.value )
            t ← t.value.decode( bundle )
        } yield field[K]( h ) :: t
    }

    implicit def decoderArrayReader[V: ClassTag](
        implicit
        r: Lazy[Reader[Bundle, V]]
    ): Decoder[Array[V]] = instance( decodeCollection( _ )( r.value ).map( _.toArray ) )

    implicit def decoderIterableReader[V, F[α] <: Iterable[α]](
        implicit
        r:   Lazy[Reader[Bundle, V]],
        cbf: CanBuildFrom[Nothing, V, F[V]]
    ): Decoder[F[V]] = instance( decodeCollection( _ )( r.value ).map( _.to[F] ) )

    implicit def decoderMapStringReader[V, M[α, β] <: Map[α, β]](
        implicit
        r:   Lazy[Reader[Bundle, V]],
        cbf: CanBuildFrom[Nothing, ( String, V ), M[String, V]]
    ): Decoder[M[String, V]] = instance { bundle ⇒
        import collection.JavaConversions._

        val builder = cbf()

        bundle.keySet()
            .map { key ⇒ ( key, bundle.read[V]( key )( r.value ) ) }
            .collect { case ( key, Some( value ) ) ⇒ ( key, value ) }
            .foreach( builder += _ )

        Some( builder.result() )
    }

    implicit def decoderMapReaderReader[K, V, M[α, β] <: Map[α, β]](
        implicit
        dk:  Lazy[Decoder[Iterable[K]]],
        dv:  Lazy[Decoder[Iterable[V]]],
        cbf: CanBuildFrom[Nothing, ( K, V ), M[K, V]]
    ): Decoder[M[K, V]] = instance { bundle ⇒
        val values = for {
            keys ← bundle.read[Bundle]( "keys" ).flatMap( dk.value.decode )
            values ← bundle.read[Bundle]( "values" ).flatMap( dv.value.decode )
        } yield keys zip values

        values map { values ⇒
            val builder = cbf()
            values.foreach( builder += _ )
            builder.result()
        }
    }

    private def decodeCollection[V]( bundle: Bundle )( implicit r: Reader[Bundle, V] ) = {
        val collection = ( 0 until bundle.size() )
            .map( _.toString )
            .map( bundle.read[V] )

        val filtered = collection.collect { case Some( value ) ⇒ value }

        if ( collection.length != filtered.length ) {
            None
        } else {
            Some( filtered )
        }
    }
}

trait Decoder1 extends Decoder2 {
    implicit def decoderCaseClass[T, LG](
        implicit
        lg: LabelledGeneric.Aux[T, LG],
        c:  Lazy[Decoder[LG]]
    ): Decoder[T] = instance( c.value.decode( _ ).map( lg.from ) )
}

trait Decoder2 extends DecoderOperations {
    implicit def decoderGeneric[T, G](
        implicit
        g: Generic.Aux[T, G],
        d: Lazy[Decoder[G]]
    ): Decoder[T] = instance( d.value.decode( _ ).map( g.from ) )
}

trait DecoderOperations {
    implicit val functorDecoder: Functor[Decoder] = new Functor[Decoder] {
        override def map[A, B]( fa: Decoder[A] )( f: A ⇒ B ): Decoder[B] = {
            instance( bundle ⇒ fa.decode( bundle ).map( f ) )
        }
    }

    def apply[V]( implicit d: Decoder[V] ): Decoder[V] = d

    def instance[V]( f: Bundle ⇒ Option[V] ): Decoder[V] = new Decoder[V] {
        override def decode( bundle: Bundle ): Option[V] = f( bundle )
    }

    def instanceNullable[V]( f: Bundle ⇒ V ): Decoder[V] = instance( bundle ⇒ Option( f( bundle ) ) )
}