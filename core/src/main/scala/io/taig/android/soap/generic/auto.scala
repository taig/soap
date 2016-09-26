package io.taig.android.soap.generic

import io.taig.android.soap._
import io.taig.android.soap.syntax.reader._
import io.taig.android.soap.syntax.writer._
import shapeless._
import shapeless.labelled._
import shapeless.syntax.singleton._

import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import scala.reflect.{ ClassTag, classTag }

trait auto extends auto0 {
    implicit val decoderCNil: Decoder[CNil] = Decoder.instance { _ ⇒
        sys.error( "No Decoder representation for CNil (this shouldn't happen)" )
    }

    implicit val encoderCNil: Encoder[CNil] = Encoder.instance { _ ⇒
        sys.error( "No Encoder representation for CNil (this shouldn't happen)" )
    }

    implicit val decoderHNil: Decoder[HNil] = Decoder.instance { _ ⇒
        Some( HNil )
    }

    implicit val encoderHNil: Encoder[HNil] = Encoder.instance { _ ⇒
        Bundle.empty
    }

    implicit def decoderCoproduct[H: ClassTag, T <: Coproduct](
        implicit
        h: Lazy[Decoder[H]],
        t: Lazy[Decoder[T]]
    ): Decoder[H :+: T] = Decoder.instance { bundle ⇒
        val name = classTag[H].runtimeClass.getCanonicalName

        bundle.read[String]( "type" ).flatMap {
            case `name` ⇒ h.value.decode( bundle ).map( Inl.apply )
            case _      ⇒ t.value.decode( bundle ).map( Inr.apply )
        }
    }

    implicit def encoderCoproduct[H: ClassTag, T <: Coproduct](
        implicit
        h: Lazy[Encoder[H]],
        t: Lazy[Encoder[T]]
    ): Encoder[H :+: T] = Encoder.instance {
        case Inl( head ) ⇒
            h.value.encode( head )
                .write( "type", classTag[H].runtimeClass.getCanonicalName )
        case Inr( tail ) ⇒ t.value.encode( tail )
    }

    implicit def decoderRecord[K <: Symbol, V, T <: HList](
        implicit
        k: Witness.Aux[K],
        h: Lazy[Reader.Bundle[V]],
        t: Lazy[Decoder[T]]
    ): Decoder[FieldType[K, V] :: T] = Decoder.instance { bundle ⇒
        for {
            h ← bundle.read[V]( k.value.name )( h.value )
            t ← t.value.decode( bundle )
        } yield field[K]( h ) :: t
    }

    implicit def encoderRecord[K <: Symbol, V, T <: HList](
        implicit
        lf: fold.F[FieldType[K, V] :: T, Bundle]
    ): Encoder[FieldType[K, V] :: T] = Encoder.instance { list ⇒
        list.foldLeft( Bundle( list.runtimeLength ) )( fold )
    }

    implicit def decoderArrayReader[V: ClassTag](
        implicit
        r: Lazy[Reader[Bundle, V]]
    ): Decoder[Array[V]] = Decoder.instance { bundle ⇒
        decodeCollection( bundle )( r.value ).map( _.toArray )
    }

    implicit def encoderArrayWriter[V](
        implicit
        w: Lazy[Writer[Bundle, V]]
    ): Encoder[Array[V]] = Encoder.instance { array ⇒
        encodeCollection[V]( array.toSeq )( w.value )
    }

    implicit def decoderIterableReader[V, F[+α] <: Iterable[α]](
        implicit
        r:   Lazy[Reader[Bundle, V]],
        cbf: CanBuildFrom[Nothing, V, F[V]]
    ): Decoder[F[V]] = Decoder.instance { bundle ⇒
        decodeCollection( bundle )( r.value ).map( _.to[F] )
    }

    implicit def encoderIterableWriter[V, F[+α] <: Iterable[α]](
        implicit
        w: Lazy[Writer[Bundle, V]]
    ): Encoder[F[V]] = Encoder.instance( encodeCollection[V]( _ )( w.value ) )

    implicit def decoderMapStringReader[V, M[α, +β] <: Map[α, β]](
        implicit
        r:   Lazy[Reader[Bundle, V]],
        cbf: CanBuildFrom[Nothing, ( String, V ), M[String, V]]
    ): Decoder[M[String, V]] = Decoder.instance { bundle ⇒
        import collection.JavaConversions._

        val builder = cbf()

        bundle.keySet()
            .map { key ⇒ ( key, bundle.read[V]( key )( r.value ) ) }
            .collect { case ( key, Some( value ) ) ⇒ ( key, value ) }
            .foreach( builder += _ )

        Some( builder.result() )
    }

    implicit def encoderMapStringWriter[V, M[α, +β] <: Map[α, β]](
        implicit
        w: Lazy[Writer[Bundle, V]]
    ): Encoder[M[String, V]] = Encoder.instance { values ⇒
        val bundle = Bundle( values.size )
        values.foreach {
            case ( key, value ) ⇒ bundle.write( key, value )( w.value )
        }
        bundle
    }

    implicit def decoderMapReaderReader[K, V, M[α, +β] <: Map[α, β]](
        implicit
        dk:  Lazy[Decoder[Iterable[K]]],
        dv:  Lazy[Decoder[Iterable[V]]],
        cbf: CanBuildFrom[Nothing, ( K, V ), M[K, V]]
    ): Decoder[M[K, V]] = Decoder.instance { bundle ⇒
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

    implicit def encoderMapEncoderEncoder[K, V, M[α, +β] <: Map[α, β]](
        implicit
        ek: Lazy[Encoder[Iterable[K]]],
        ev: Lazy[Encoder[Iterable[V]]]
    ): Encoder[M[K, V]] = Encoder.instance { values ⇒
        Bundle( 2 ).write(
            "keys" ->> ek.value.encode( values.keys ) ::
                "values" ->> ev.value.encode( values.values ) ::
                HNil
        )
    }

    private def decodeCollection[V]( bundle: Bundle )(
        implicit
        r: Reader[Bundle, V]
    ) = {
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

    private def encodeCollection[V]( values: Iterable[V] )(
        implicit
        w: Writer[Bundle, V]
    ): Bundle = {
        val bundle = Bundle( values.size )
        values.zipWithIndex.foreach {
            case ( value, index ) ⇒ bundle.write( index.toString, value )
        }
        bundle
    }
}

trait auto0 extends auto1 {
    implicit def decoderCaseClass[T, LG](
        implicit
        lg: LabelledGeneric.Aux[T, LG],
        c:  Lazy[Decoder[LG]]
    ): Decoder[T] = Decoder.instance( c.value.decode( _ ).map( lg.from ) )

    implicit def encoderCaseClass[T, LG](
        implicit
        lg: LabelledGeneric.Aux[T, LG],
        e:  Lazy[Encoder[LG]]
    ): Encoder[T] = Encoder.instance { value ⇒
        e.value.encode( lg.to( value ) )
    }
}

trait auto1 {
    implicit def decoderGeneric[T, G](
        implicit
        g: Generic.Aux[T, G],
        d: Lazy[Decoder[G]]
    ): Decoder[T] = Decoder.instance( d.value.decode( _ ).map( g.from ) )

    implicit def encoderGeneric[T, G](
        implicit
        g: Generic.Aux[T, G],
        e: Lazy[Encoder[G]]
    ): Encoder[T] = Encoder.instance { value ⇒
        e.value.encode( g.to( value ) )
    }
}

object auto extends auto