package io.taig.android.parcelable.bundle

import io.taig.android.parcelable
import io.taig.android.parcelable._
import io.taig.android.parcelable.functional._
import shapeless.Lazy

import scala.language.higherKinds

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

trait Decoders0 extends DecoderOperations {
    implicit def `Decoder[bundler.Decoder]`[V]( implicit d: Lazy[bundler.Decoder[V]] ): Decoder[V] = Decoder.instance {
        case ( bundle, key ) ⇒ d.value.decode( bundle.read[Bundle]( key ) )
    }
}

trait DecoderOperations {
    def apply[V: Decoder]: Decoder[V] = implicitly[Decoder[V]]

    def instance[V]( f: ( Bundle, String ) ⇒ V ): Decoder[V] = new Decoder[V] {
        override def decodeRaw( serialization: ( Bundle, String ) ) = f.tupled( serialization )
    }

    implicit val `Map[Decoder]`: Map[Decoder] = new Map[Decoder] {
        override def map[A, B]( b: Decoder[A] )( f: A ⇒ B ) = new Decoder[B] {
            override def decodeRaw( serialization: ( Bundle, String ) ) = f( b.decode( serialization ) )
        }
    }
}