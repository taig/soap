package io.taig.android.parcelable.bundle

import export.imports
import io.taig.android.parcelable
import io.taig.android.parcelable._
import io.taig.android.parcelable.functional._

trait Decoder[V] extends parcelable.Decoder[( Bundle, String ), V] {
    override def decode( serialization: ( Bundle, String ) ): V = serialization match {
        case ( bundle, key ) ⇒ bundle.containsKey( key ) match {
            case true  ⇒ decodeRaw( serialization )
            case false ⇒ throw exception.KeyNotFound( key )
        }
    }

    def decodeRaw( serialization: ( Bundle, String ) ): V
}

object Decoder extends DecoderOperations with Decoders0

@imports[Decoder]
trait Decoders0 extends DecoderOperations

trait DecoderOperations {
    def apply[V: Decoder]: Decoder[V] = implicitly[Decoder[V]]

    def instance[V]( f: ( Bundle, String ) ⇒ V ): Decoder[V] = new Decoder[V] {
        override def decodeRaw( serialization: ( Bundle, String ) ) = f.tupled( serialization )
    }

    implicit val `Functor[Decoder]`: Functor[Decoder] = new Functor[Decoder] {
        override def map[A, B]( b: Decoder[A] )( f: A ⇒ B ) = instance {
            case serialization ⇒ f( b.decode( serialization ) )
        }
    }
}