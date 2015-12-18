package io.taig.android.parcelable.intent

import java.net.URL

import android.content.Intent
import android.os.Parcelable
import export.exports
import io.taig.android.parcelable
import io.taig.android.parcelable._
import io.taig.android.parcelable.functional._
import io.taig.android.parcelable.syntax._
import julienrf.enum.Enum
import shapeless.Lazy

import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import scala.reflect.ClassTag

trait Codec[V]
    extends parcelable.Codec[( Intent, String, V ), Unit, ( Intent, String ), V]
    with Encoder[V]
    with Decoder[V]

@exports
object Codec extends CodecOperations with Codecs0

trait Codecs0 extends CodecOperations

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