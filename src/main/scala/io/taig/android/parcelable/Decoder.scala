package io.taig.android.parcelable

trait Decoder extends Codec {
    override type Input = Serialization

    override type Output = Value

    def decode( serialization: Input ): Output
}

object Decoder {
    type Aux[I, O] = Decoder { type Serialization = I; type Value = O }
}