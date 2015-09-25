package test.io.taig.android.parcelable

import io.taig.android.Parcelable

@Parcelable
case class Primitive( a: String, b: Int, c: Double ) extends Parent[String]

object Primitive {
    val default = Primitive( "asdf", 5, 11.11 )
}