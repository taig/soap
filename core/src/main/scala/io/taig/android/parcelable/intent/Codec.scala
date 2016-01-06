package io.taig.android.parcelable.intent

import android.content.Intent
import io.taig.android.parcelable
import io.taig.android.parcelable.functional._
import shapeless.Lazy

import scala.language.higherKinds

trait Codec[V]
    extends parcelable.Codec[( Intent, String, V ), Unit, ( Intent, String ), V]
    with Encoder[V]
    with Decoder[V]

object Codec extends CodecOperations with Codecs0

trait Codecs0 extends CodecOperations {
    implicit def `Codec[Encoder, Decoder]`[V]( implicit e: Lazy[Encoder[V]], d: Lazy[Decoder[V]] ): Codec[V] = {
        Codec.instance(
            { case value ⇒ e.value.encode( value ) },
            { case serialization ⇒ d.value.decode( serialization ) }
        )
    }
}

trait CodecOperations {
    def apply[V: Codec]: Codec[V] = implicitly[Codec[V]]

    def instance[V]( e: ( Intent, String, V ) ⇒ Unit, d: ( Intent, String ) ⇒ V ): Codec[V] = new Codec[V] {
        override def encode( value: ( Intent, String, V ) ) = e.tupled( value )

        override def decodeRaw( serialization: ( Intent, String ) ) = d.tupled( serialization )
    }

    implicit val `Inmap[Codec]`: Inmap[Codec] = new Inmap[Codec] {
        override def inmap[A, B]( fa: Codec[A] )( contramap: B ⇒ A, map: A ⇒ B ) = instance(
            { case value ⇒ implicitly[Contravariant[Encoder]].contramap( fa )( contramap ).encode( value ) },
            { case serialization ⇒ implicitly[Functor[Decoder]].map( fa )( map ).decode( serialization ) }
        )
    }
}