package io.taig.android.parcelable.intent

import android.content.Intent
import cats.Functor
import export.imports
import io.taig.android.parcelable
import io.taig.android.parcelable._

trait Decoder[V] extends parcelable.Decoder {
    override type Serialization = ( Intent, String )

    override type Value = V

    override def decode( serialization: Serialization ): V = serialization match {
        case ( intent, key ) ⇒
            if ( intent.hasExtra( key ) ) {
                decodeRaw( serialization )
            } else {
                throw exception.KeyNotFound( key )
            }
    }

    def decodeRaw( serialization: Serialization ): V
}

object Decoder extends Decoders {
    def apply[V]( f: ( Intent, String ) ⇒ V ): Decoder[V] = new Decoder[V] {
        override def decodeRaw( serialization: ( Intent, String ) ) = f.tupled( serialization )
    }

    implicit val `Functor[Bundle]`: Functor[Decoder] = new Functor[Decoder] {
        override def map[A, B]( b: Decoder[A] )( f: A ⇒ B ) = new Decoder[B] {
            override def decodeRaw( serialization: ( Intent, String ) ) = f( b.decode( serialization ) )
        }
    }
}

@imports[Decoder]
trait Decoders extends LowPriorityDecoders

trait LowPriorityDecoders