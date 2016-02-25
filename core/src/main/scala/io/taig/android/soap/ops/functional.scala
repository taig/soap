package io.taig.android.soap.ops

import io.taig.android.soap.functional.{ Contravariant, Functor, Inmap }

import scala.language.higherKinds

class contramap[A, F[_]: Contravariant]( fa: F[A] ) {
    def contramap[B]( f: B ⇒ A ): F[B] = implicitly[Contravariant[F]].contramap( fa )( f )
}

class inmap[A, F[_]: Inmap]( fa: F[A] ) {
    def inmap[B]( contramap: B ⇒ A, map: A ⇒ B ): F[B] = implicitly[Inmap[F]].inmap( fa )( contramap, map )
}

class map[A, F[_]: Functor]( fa: F[A] ) {
    def map[B]( f: A ⇒ B ): F[B] = implicitly[Functor[F]].map( fa )( f )
}