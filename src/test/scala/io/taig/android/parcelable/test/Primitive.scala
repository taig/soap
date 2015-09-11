package io.taig.android.parcelable.test

import io.taig.android.Parcelable

@Parcelable
case class Primitive( a: String, b: Int, c: Double )

object Primitive {
    val default = Primitive( "asdf", 5, 11.11 )
}