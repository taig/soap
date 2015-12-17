package io.taig.android.parcelable.bundler

import io.taig.android.parcelable
import io.taig.android.parcelable._
import io.taig.android.parcelable.functional._

import scala.language.higherKinds

trait Decoder[V] extends parcelable.Decoder[Bundle, V]

object Decoder extends DecoderOperations with Decoders0

trait Decoders0 extends DecoderOperations

trait DecoderOperations {
    def apply[V: Decoder]: Decoder[V] = implicitly[Decoder[V]]

    def instance[V]( f: Bundle ⇒ V ): Decoder[V] = new Decoder[V] {
        override def decode( bundle: Bundle ) = f( bundle )
    }

    implicit val `Map[Decoder]`: Map[Decoder] = new Map[Decoder] {
        override def map[A, B]( b: Decoder[A] )( f: A ⇒ B ) = new Decoder[B] {
            override def decode( serialization: Bundle ) = f( b.decode( serialization ) )
        }
    }
}