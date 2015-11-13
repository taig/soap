package io.taig.android.parcelable.internal

import android.os.Parcel
import io.taig.android.parcelable._

import scala.language.{ higherKinds, implicitConversions }

trait Functor[F[_]] {
    def map[A, B]( fa: F[A] )( f: A ⇒ B ): F[B]
}

object Functor {
    implicit def `Functor[bundleable.Read]`: Functor[bundleable.Read] = new Functor[bundleable.Read] {
        override def map[A, B]( fa: bundleable.Read[A] )( f: A ⇒ B ) = new bundleable.Read[B] {
            override def read( bundle: Bundle ) = f( fa.read( bundle ) )
        }
    }

    implicit def `Functor[bundleize.Read]`: Functor[bundleize.Read] = new Functor[bundleize.Read] {
        override def map[A, B]( fa: bundleize.Read[A] )( f: A ⇒ B ) = new bundleize.Read[B] {
            override def read( bundle: Bundle, key: String ) = f( fa.read( bundle, key ) )
        }
    }

    implicit def `Functor[parcelize.Read]`: Functor[parcelize.Read] = new Functor[parcelize.Read] {
        override def map[A, B]( fa: parcelize.Read[A] )( f: A ⇒ B ) = new parcelize.Read[B] {
            override def read( source: Parcel ) = f( fa.read( source ) )
        }
    }
}