package io.taig.android.parcelable.bundleize

import io.taig.android.parcelable._
import io.taig.android.parcelable.bundleize.read.Default

/**
 * Type class that instructs how to read a value from a given Bundle
 */
trait Read[T] {
    def read( bundle: Bundle, key: String ): T
}

object Read extends Default {
    def apply[T]( f: ( Bundle, String ) ⇒ T ) = new Read[T] {
        override def read( bundle: Bundle, key: String ) = bundle.checked( key )( f )
    }
}