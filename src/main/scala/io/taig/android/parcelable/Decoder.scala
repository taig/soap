package io.taig.android.parcelable

trait Decoder[I, O] {
    def decode( serialization: I ): O
}