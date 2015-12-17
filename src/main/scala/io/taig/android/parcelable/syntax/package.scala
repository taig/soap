package io.taig.android.parcelable

import io.taig.android.parcelable.functional.{ Inmap, Contramap, Map }

import scala.language.higherKinds

package object syntax {
    implicit class RichContramap[A, F[_]: Contramap]( fa: F[A] ) {
        def contramap[B]( f: B ⇒ A ): F[B] = implicitly[Contramap[F]].contramap( fa )( f )
    }

    implicit class RichInmap[A, F[_]: Inmap]( fa: F[A] ) {
        def inmap[B]( map: A ⇒ B, contramap: B ⇒ A ): F[B] = implicitly[Inmap[F]].inmap( fa )( map, contramap )
    }

    implicit class RichMap[A, F[_]: Map]( fa: F[A] ) {
        def map[B]( f: A ⇒ B ): F[B] = implicitly[Map[F]].map( fa )( f )
    }
}