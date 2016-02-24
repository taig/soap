package io.taig.android.parcelable.syntax

import io.taig.android.parcelable.functional._
import io.taig.android.parcelable.ops

import scala.language.{ higherKinds, implicitConversions }

trait functional {
    implicit def contramapSyntax[A, F[_]: Contravariant]( fa: F[A] ): ops.contramap[A, F] = {
        new ops.contramap[A, F]( fa )
    }

    implicit def inmapSyntax[A, F[_]: Inmap]( fa: F[A] ): ops.inmap[A, F] = new ops.inmap[A, F]( fa )

    implicit def mapSyntax[A, F[_]: Functor]( fa: F[A] ): ops.map[A, F] = new ops.map[A, F]( fa )
}

object functional extends functional