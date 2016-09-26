package io.taig.android.soap

import cats.functor.Contravariant

import scala.language.higherKinds

/**
 * Type class which describes how to convert a value V to a Bundle
 */
trait Encoder[V] {
    def encode( value: V ): Bundle
}

object Encoder {
    def apply[V]( implicit e: Encoder[V] ): Encoder[V] = e

    def instance[V]( f: V ⇒ Bundle ): Encoder[V] = new Encoder[V] {
        override def encode( value: V ): Bundle = f( value )
    }

    implicit val contravariantEncoder: Contravariant[Encoder] = {
        new Contravariant[Encoder] {
            override def contramap[A, B]( fa: Encoder[A] )( f: B ⇒ A ): Encoder[B] = {
                instance( value ⇒ fa.encode( f( value ) ) )
            }
        }
    }
}