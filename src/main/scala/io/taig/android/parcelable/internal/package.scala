package io.taig.android.parcelable

import scala.language.higherKinds

package object internal {
    implicit class RichFunctor[F[_], A]( fa: F[A] )( implicit functor: Functor[F] ) {
        def map[B]( f: A ⇒ B ): F[B] = functor.map( fa )( f )
    }
}