package io.taig.android.parcelable

import io.taig.android.parcelable.{ Bundle ⇒ ABundle }

trait Decoder extends Codec {
    override type Input = Serialization

    override type Output = Value

    def decode( serialization: Input ): Output
}

object Decoder extends Decoders {
    type Aux[I, O] = Decoder { type Serialization = I; type Value = O }

    trait Bundle[V] extends Decoder {
        override type Serialization = ( ABundle, String )

        override type Value = V
    }
}

trait Decoders