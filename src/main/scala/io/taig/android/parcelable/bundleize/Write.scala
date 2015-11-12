package io.taig.android.parcelable.bundleize

import io.taig.android.parcelable._
import io.taig.android.parcelable.bundleize.write.Default

/**
 * Type class that instructs how to read/write a value from/to a given Bundle
 */
trait Write[-T] {
    def write( bundle: Bundle, key: String, value: T ): Unit
}

object Write extends Default {
    def apply[T]( f: ( Bundle, String, T ) ⇒ Unit ) = new Write[T] {
        override def write( bundle: Bundle, key: String, value: T ) = f( bundle, key, value )
    }
}