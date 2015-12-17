package io.taig.android.parcelable.bundler

import io.taig.android.parcelable
import io.taig.android.parcelable._
import io.taig.android.parcelable.functional._

import scala.language.higherKinds

trait Encoder[V] extends parcelable.Encoder[V, Bundle]

object Encoder extends EncoderOperations with Encoders0

trait Encoders0 extends EncoderOperations

trait EncoderOperations {
    def apply[V: Encoder]: Encoder[V] = implicitly[Encoder[V]]

    def instance[V]( f: V ⇒ Bundle ): Encoder[V] = new Encoder[V] {
        override def encode( value: V ) = f( value )
    }

    implicit val `Contramap[Encoder]`: Contramap[Encoder] = new Contramap[Encoder] {
        override def contramap[A, B]( b: Encoder[A] )( f: B ⇒ A ) = new Encoder[B] {
            override def encode( value: B ) = b.encode( f( value ) )
        }
    }
}