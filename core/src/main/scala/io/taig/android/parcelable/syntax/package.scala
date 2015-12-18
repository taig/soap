package io.taig.android.parcelable

import io.taig.android.parcelable.functional.{ Contravariant, Functor, Inmap }

import scala.language.higherKinds

package object syntax {
    implicit class RichContramap[A, F[_]: Contravariant]( fa: F[A] ) {
        def contramap[B]( f: B ⇒ A ): F[B] = implicitly[Contravariant[F]].contramap( fa )( f )
    }

    implicit class RichInmap[A, F[_]: Inmap]( fa: F[A] ) {
        def inmap[B]( contramap: B ⇒ A, map: A ⇒ B ): F[B] = implicitly[Inmap[F]].inmap( fa )( contramap, map )
    }

    implicit class RichMap[A, F[_]: Functor]( fa: F[A] ) {
        def map[B]( f: A ⇒ B ): F[B] = implicitly[Functor[F]].map( fa )( f )
    }
}