package io.taig.android.soap.functional

import scala.language.higherKinds

trait Inmap[F[_]] {
    def inmap[A, B]( fa: F[A] )( contramap: B ⇒ A, map: A ⇒ B ): F[B]
}