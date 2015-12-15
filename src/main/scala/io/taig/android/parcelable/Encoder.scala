package io.taig.android.parcelable

trait Encoder extends Codec {
    override type Input = Value

    override type Output = Serialization

    def encode( value: Input ): Output
}

object Encoder {
    type Aux[I, O] = Encoder { type Value = I; type Serialization = O }
}