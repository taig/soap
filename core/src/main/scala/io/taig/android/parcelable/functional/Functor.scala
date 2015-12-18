package io.taig.android.parcelable.functional

import scala.language.higherKinds

trait Functor[F[_]] {
    def map[A, B]( fa: F[A] )( f: A ⇒ B ): F[B]
}