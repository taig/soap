package io.taig.android.soap.intent

import android.content.Intent
import io.taig.android.soap
import shapeless.Lazy

import scala.language.higherKinds

trait Codec[V]
    extends soap.Codec[( Intent, String, V ), Unit, ( Intent, String ), V]
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

    def instance[V]( e: ( Intent, String, V ) ⇒ Unit, d: ( Intent, String ) ⇒ V ): Codec[V] = new Codec[V] {
        override def encode( value: ( Intent, String, V ) ) = e.tupled( value )

        override def decodeRaw( serialization: ( Intent, String ) ) = d.tupled( serialization )
    }
}