package io.taig.android.parcelable.intent

import android.content.Intent
import cats.functor.Contravariant
import io.taig.android.parcelable

trait Encoder[V] extends parcelable.Encoder {
    override type Value = ( Intent, String, V )

    override type Serialization = Unit
}

object Encoder extends Encoders {
    def apply[V]( f: ( Intent, String, V ) ⇒ Unit ): Encoder[V] = new Encoder[V] {
        override def encode( value: ( Intent, String, V ) ) = f.tupled( value )
    }

    implicit val `Contravariant[Bundle]` = new Contravariant[Encoder] {
        override def contramap[A, B]( b: Encoder[A] )( f: B ⇒ A ) = new Encoder[B] {
            override def encode( value: ( Intent, String, B ) ) = b.encode( value.copy( _3 = f( value._3 ) ) )
        }
    }
}

trait Encoders extends LowPriorityEncoders

trait LowPriorityEncoders