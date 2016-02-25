package io.taig.android.soap

trait Encoder[I, O] {
    def encode( value: I ): O
}