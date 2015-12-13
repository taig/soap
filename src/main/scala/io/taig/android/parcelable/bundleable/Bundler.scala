package io.taig.android.parcelable.bundleable

/**
 * Type class that describes how to convert a type T to a Bundle and vice-versa
 */
trait Bundler[T] {
    /**
     * Create a Bundle representation of type T
     */
    def bundle( value: T ): Bundle

    /**
     * Create an instance of type T from a Bundle
     */
    def unbundle( bundle: Bundle ): T
}

object Bundler extends Bundlers {
    def apply[T]( b: T ⇒ Bundle, ub: Bundle ⇒ T ) = new Bundler[T] {
        override def bundle( value: T ) = b( value )

        override def unbundle( bundle: Bundle ) = ub( bundle )
    }
}