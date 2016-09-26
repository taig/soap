package io.taig.android.soap

import cats.Functor

import scala.language.higherKinds

/**
 * Type class which describes how to convert a Bundle to a value V
 */
trait Decoder[V] {
    def decode( bundle: Bundle ): Option[V]
}

object Decoder {
    def apply[V]( implicit d: Decoder[V] ): Decoder[V] = d

    def instance[V]( f: Bundle ⇒ Option[V] ): Decoder[V] = new Decoder[V] {
        override def decode( bundle: Bundle ): Option[V] = f( bundle )
    }

    def instanceNullable[V]( f: Bundle ⇒ V ): Decoder[V] = instance { bundle ⇒
        Option( f( bundle ) )
    }

    implicit val functorDecoder: Functor[Decoder] = new Functor[Decoder] {
        override def map[A, B]( fa: Decoder[A] )( f: A ⇒ B ): Decoder[B] = {
            instance( bundle ⇒ fa.decode( bundle ).map( f ) )
        }
    }
}