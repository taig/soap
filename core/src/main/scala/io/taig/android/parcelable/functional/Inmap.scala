package io.taig.android.parcelable.functional

import scala.language.higherKinds

trait Inmap[F[_]] {
    def inmap[A, B]( fa: F[A] )( map: A ⇒ B, contramap: B ⇒ A ): F[B]
}