package io.taig.android.soap.functional

import scala.language.higherKinds

trait Contravariant[F[_]] {
    def contramap[A, B]( fa: F[A] )( f: B ⇒ A ): F[B]
}