package io.taig.android.parcelable

trait Codec {
    type Input

    type Output

    type Serialization

    type Value
}

object Codec