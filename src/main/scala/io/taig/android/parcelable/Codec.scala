package io.taig.android.parcelable

trait Codec[EI, EO, DI, DO] extends Encoder[EI, EO] with Decoder[DI, DO]

object Codec {
    type Symmetric[Serialization, Value] = Codec[Value, Serialization, Serialization, Value]
}