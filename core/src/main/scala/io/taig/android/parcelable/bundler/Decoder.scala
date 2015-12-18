package io.taig.android.parcelable.bundler

import export.imports
import io.taig.android.parcelable
import io.taig.android.parcelable._
import io.taig.android.parcelable.functional._

import scala.language.higherKinds

trait Decoder[V] extends parcelable.Decoder[Bundle, V]

object Decoder extends DecoderOperations with Decoders0

@imports[Decoder]
trait Decoders0 extends DecoderOperations

trait DecoderOperations {
    def apply[V: Decoder]: Decoder[V] = implicitly[Decoder[V]]

    def instance[V]( f: Bundle ⇒ V ): Decoder[V] = new Decoder[V] {
        override def decode( bundle: Bundle ) = f( bundle )
    }

    implicit val `Functor[Decoder]`: Functor[Decoder] = new Functor[Decoder] {
        override def map[A, B]( b: Decoder[A] )( f: A ⇒ B ) = instance( bundle ⇒ f( b.decode( bundle ) ) )
    }
}