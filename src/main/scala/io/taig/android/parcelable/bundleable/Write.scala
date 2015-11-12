package io.taig.android.parcelable.bundleable

import io.taig.android.parcelable._
import io.taig.android.parcelable.bundleable.write.Default

import scala.language.higherKinds

/**
 * Type class that instructs how to serialize a value to a Bundle
 */
trait Write[-T] {
    def write( value: T ): Bundle
}

object Write extends Default {
    def apply[T]( f: T ⇒ Bundle ) = new Write[T] {
        override def write( value: T ) = f( value )
    }
}