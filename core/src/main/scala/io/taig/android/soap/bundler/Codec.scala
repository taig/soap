package io.taig.android.soap.bundler

import io.taig.android.soap
import io.taig.android.soap._
import shapeless.Lazy

import scala.language.higherKinds

trait Codec[V]
    extends soap.Codec.Symmetric[Bundle, V]
    with Encoder[V]
    with Decoder[V]

object Codec extends CodecOperations with Codecs0

trait Codecs0 extends CodecOperations {
    implicit def codecEncoderDecoder[V]( implicit e: Lazy[Encoder[V]], d: Lazy[Decoder[V]] ): Codec[V] = {
        Codec.instance(
            { case value ⇒ e.value.encode( value ) },
            { case serialization ⇒ d.value.decode( serialization ) }
        )
    }
}

trait CodecOperations {
    def apply[V: Codec]: Codec[V] = implicitly[Codec[V]]

    def instance[V]( e: V ⇒ Bundle, d: Bundle ⇒ V ): Codec[V] = new Codec[V] {
        override def encode( value: V ) = e( value )

        override def decode( serialization: Bundle ) = d( serialization )
    }
}