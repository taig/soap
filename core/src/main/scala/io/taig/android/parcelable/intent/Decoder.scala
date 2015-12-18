package io.taig.android.parcelable.intent

import android.content.Intent
import export.imports
import io.taig.android.parcelable
import io.taig.android.parcelable._
import io.taig.android.parcelable.functional._

import scala.language.higherKinds

trait Decoder[V] extends parcelable.Decoder[( Intent, String ), V] {
    override def decode( serialization: ( Intent, String ) ): V = serialization match {
        case ( intent, key ) ⇒ intent.hasExtra( key ) match {
            case true  ⇒ decodeRaw( serialization )
            case false ⇒ throw exception.KeyNotFound( key )
        }
    }

    def decodeRaw( serialization: ( Intent, String ) ): V
}

object Decoder extends DecoderOperations with Decoders0

@imports[Decoder]
trait Decoders0 extends DecoderOperations

trait DecoderOperations {
    def apply[V: Decoder]: Decoder[V] = implicitly[Decoder[V]]

    def instance[V]( f: ( Intent, String ) ⇒ V ): Decoder[V] = new Decoder[V] {
        override def decodeRaw( serialization: ( Intent, String ) ) = f.tupled( serialization )
    }

    implicit val `Map[Decoder]`: Functor[Decoder] = new Functor[Decoder] {
        override def map[A, B]( b: Decoder[A] )( f: A ⇒ B ) = instance {
            case serialization ⇒ f( b.decode( serialization ) )
        }
    }
}