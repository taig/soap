package io.taig.android.parcelable.bundler

import export.imports
import io.taig.android.parcelable
import io.taig.android.parcelable._
import io.taig.android.parcelable.functional._

import scala.language.higherKinds

trait Encoder[V] extends parcelable.Encoder[V, Bundle]

object Encoder extends EncoderOperations with Encoders0

@imports[Encoder]
trait Encoders0 extends EncoderOperations

trait EncoderOperations {
    def apply[V: Encoder]: Encoder[V] = implicitly[Encoder[V]]

    def instance[V]( f: V ⇒ Bundle ): Encoder[V] = new Encoder[V] {
        override def encode( value: V ) = f( value )
    }

    implicit val `Contravariant[Encoder]`: Contravariant[Encoder] = new Contravariant[Encoder] {
        override def contramap[A, B]( b: Encoder[A] )( f: B ⇒ A ) = instance( value ⇒ b.encode( f( value ) ) )
    }
}