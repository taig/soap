package io.taig.android.parcelable.intent

import android.content.Intent
import export.imports
import io.taig.android.parcelable
import io.taig.android.parcelable.functional._

import scala.language.higherKinds

trait Encoder[V] extends parcelable.Encoder[( Intent, String, V ), Unit]

object Encoder extends EncoderOperations with Encoders0

@imports[Encoder]
trait Encoders0 extends EncoderOperations

trait EncoderOperations {
    def apply[V: Encoder]: Encoder[V] = implicitly[Encoder[V]]

    def instance[V]( f: ( Intent, String, V ) ⇒ Unit ): Encoder[V] = new Encoder[V] {
        override def encode( value: ( Intent, String, V ) ) = f.tupled( value )
    }

    implicit val `Contramap[Encoder]`: Contravariant[Encoder] = new Contravariant[Encoder] {
        override def contramap[A, B]( b: Encoder[A] )( f: B ⇒ A ) = instance {
            case ( intent, key, value ) ⇒ b.encode( intent, key, f( value ) )
        }
    }
}