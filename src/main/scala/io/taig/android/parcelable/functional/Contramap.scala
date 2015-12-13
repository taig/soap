package io.taig.android.parcelable.functional

import scala.language.higherKinds

trait Contramap[F[_]] {
    def contramap[A, B]( fa: F[A] )( f: B ⇒ A ): F[B]
}