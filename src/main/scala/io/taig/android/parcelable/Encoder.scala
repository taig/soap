package io.taig.android.parcelable

trait Encoder extends Codec {
    override type Input = Value

    override type Output = Serialization

    def encode( value: Input ): Output
}

object Encoder extends Encoders {
    type Aux[I, O] = Encoder { type Value = I; type Serialization = O }
}

trait Encoders