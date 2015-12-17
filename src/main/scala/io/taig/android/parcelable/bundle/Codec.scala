package io.taig.android.parcelable.bundle

import io.taig.android.parcelable.Bundle

object Codec {
    def apply[V: Encoder: Decoder]: Encoder[V] with Decoder[V] = new Encoder[V] with Decoder[V] {
        override def encode( value: ( Bundle, String, V ) ) = implicitly[Encoder[V]].encode( value )

        override def decodeRaw( serialization: ( Bundle, String ) ) = implicitly[Decoder[V]].decodeRaw( serialization )
    }
}