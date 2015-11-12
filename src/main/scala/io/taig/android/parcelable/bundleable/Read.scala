package io.taig.android.parcelable.bundleable

import io.taig.android.parcelable.Bundle
import io.taig.android.parcelable.bundleable.read.Default

/**
 * Type class that instructs how to deserialize a value from a Bundle
 */
trait Read[T] {
    def read( bundle: Bundle ): T

    def map[S]( f: T ⇒ S ): Read[S] = Read( bundle ⇒ f( read( bundle ) ) )
}

object Read extends Default {
    def apply[T]( f: Bundle ⇒ T ) = new Read[T] {
        override def read( bundle: Bundle ) = f( bundle )
    }
}