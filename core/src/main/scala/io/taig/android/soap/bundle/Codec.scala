package io.taig.android.soap.bundle

import io.taig.android.soap
import io.taig.android.soap._
import io.taig.android.soap.functional._
import shapeless.Lazy

import scala.language.higherKinds

trait Codec[V]
    extends soap.Codec[( Bundle, String, V ), Unit, ( Bundle, String ), V]
    with Encoder[V]
    with Decoder[V]

object Codec extends CodecOperations with Codecs0

trait Codecs0 extends CodecOperations {
    implicit def `Codec[Encoder, Decoder]`[V]( implicit e: Lazy[Encoder[V]], d: Lazy[Decoder[V]] ): Codec[V] = {
        Codec.instance(
            { case value ⇒ e.value.encode( value ) },
            { case serialization ⇒ d.value.decode( serialization ) }
        )
    }
}

trait CodecOperations {
    def apply[V: Codec]: Codec[V] = implicitly[Codec[V]]

    def instance[V]( e: ( Bundle, String, V ) ⇒ Unit, d: ( Bundle, String ) ⇒ V ): Codec[V] = new Codec[V] {
        override def encode( value: ( Bundle, String, V ) ) = e.tupled( value )

        override def decodeRaw( serialization: ( Bundle, String ) ) = d.tupled( serialization )
    }

    implicit val `Inmap[Codec]`: Inmap[Codec] = new Inmap[Codec] {
        override def inmap[A, B]( fa: Codec[A] )( contramap: B ⇒ A, map: A ⇒ B ) = instance(
            { case value ⇒ implicitly[Contravariant[Encoder]].contramap( fa )( contramap ).encode( value ) },
            { case serialization ⇒ implicitly[Functor[Decoder]].map( fa )( map ).decode( serialization ) }
        )
    }
}