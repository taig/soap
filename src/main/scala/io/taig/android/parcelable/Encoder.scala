package io.taig.android.parcelable

trait Encoder[I, O] {
    def encode( value: I ): O
}