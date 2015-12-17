package io.taig.android.parcelable.intent

import android.content.Intent

object Codec {
    def apply[V: Encoder: Decoder]: Encoder[V] with Decoder[V] = new Encoder[V] with Decoder[V] {
        override def encode( value: ( Intent, String, V ) ) = implicitly[Encoder[V]].encode( value )

        override def decodeRaw( serialization: ( Intent, String ) ) = implicitly[Decoder[V]].decodeRaw( serialization )
    }
}