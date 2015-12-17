package io.taig.android.parcelable.bundler

import io.taig.android.parcelable.Bundle

object Codec {
    def apply[V: Encoder: Decoder]: Encoder[V] with Decoder[V] = new Encoder[V] with Decoder[V] {
        override def encode( value: V ) = implicitly[Encoder[V]].encode( value )

        override def decode( serialization: Bundle ) = implicitly[Decoder[V]].decode( serialization )
    }
}