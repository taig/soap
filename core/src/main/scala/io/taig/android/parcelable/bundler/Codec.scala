package io.taig.android.parcelable.bundler

import export.exports
import io.taig.android.parcelable
import io.taig.android.parcelable._
import io.taig.android.parcelable.functional._

import scala.language.higherKinds

trait Codec[V]
    extends parcelable.Codec.Symmetric[Bundle, V]
    with Encoder[V]
    with Decoder[V]

@exports
object Codec extends CodecOperations with Codecs0

trait Codecs0 extends CodecOperations

trait CodecOperations {
    def apply[V: Codec]: Codec[V] = implicitly[Codec[V]]

    def instance[V]( e: V ⇒ Bundle, d: Bundle ⇒ V ): Codec[V] = new Codec[V] {
        override def encode( value: V ) = e( value )

        override def decode( serialization: Bundle ) = d( serialization )
    }

    implicit val `Inmap[Codec]`: Inmap[Codec] = new Inmap[Codec] {
        override def inmap[A, B]( fa: Codec[A] )( contramap: B ⇒ A, map: A ⇒ B ) = instance(
            { case value ⇒ implicitly[Contravariant[Encoder]].contramap( fa )( contramap ).encode( value ) },
            { case serialization ⇒ implicitly[Functor[Decoder]].map( fa )( map ).decode( serialization ) }
        )
    }
}