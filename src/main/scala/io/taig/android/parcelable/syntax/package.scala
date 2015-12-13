package io.taig.android.parcelable

import io.taig.android.parcelable.functional.{ Contramap, Map }

import scala.language.higherKinds

package object syntax {
    implicit class RichContramap[A, F[_]: Contramap]( c: F[A] ) {
        def contramap[B]( f: B ⇒ A ): F[B] = implicitly[Contramap[F]].contramap( c )( f )
    }

    implicit class RichMap[A, F[_]: Map]( m: F[A] ) {
        def map[B]( f: A ⇒ B ): F[B] = implicitly[Map[F]].map( m )( f )
    }
}