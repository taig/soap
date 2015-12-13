package io.taig.android.parcelable.bundler

import io.taig.android.parcelable
import io.taig.android.parcelable.Bundle

trait Codec[V] extends parcelable.Codec {
    override type Serialization = Bundle

    override type Value = V
}