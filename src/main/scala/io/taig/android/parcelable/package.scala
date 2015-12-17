package io.taig.android

import android.content.Intent
import shapeless.HList
import shapeless.ops.hlist.LeftFolder

import scala.language.higherKinds

package object parcelable {
    type Bundle = android.os.Bundle

    trait ParcelableCodec[H] {
        protected def host: H
    }

    abstract class ParcelableEncoder[H, I[V], E[V] <: Encoder[I[V], _]] extends ParcelableCodec[H] {
        protected def encode[V]( key: String, value: V ): I[V]

        def write[V: E]( key: String, value: V ): H = {
            implicitly[E[V]].encode( encode[V]( key, value ) )
            host
        }
    }

    trait ParcelableDecoder[H, I, D[T] <: Decoder[I, T]] extends ParcelableCodec[H] {
        protected def decode[T]( key: String ): I

        def read[T: D]( key: String ): T = implicitly[D[T]].decode( decode( key ) )
    }

    implicit class ParcelableBundle( val host: Bundle )
            extends ParcelableEncoder[Bundle, ( { type λ[ɣ] = ( Bundle, String, ɣ ) } )#λ, bundle.Encoder]
            with ParcelableDecoder[Bundle, ( Bundle, String ), bundle.Decoder] {
        override protected def encode[T]( key: String, value: T ) = ( host, key, value )

        override protected def decode[T]( key: String ) = ( host, key )

        def write[L <: HList]( arguments: L )( implicit lf: LeftFolder.Aux[L, Bundle, bundle.fold.type, Bundle] ) = {
            arguments.foldLeft( host )( bundle.fold )
        }
    }

    implicit class ParcelableIntent( val host: Intent )
            extends ParcelableEncoder[Intent, ( { type λ[ɣ] = ( Intent, String, ɣ ) } )#λ, intent.Encoder]
            with ParcelableDecoder[Intent, ( Intent, String ), intent.Decoder] {
        override protected def encode[V]( key: String, value: V ) = ( host, key, value )

        override protected def decode[T]( key: String ) = ( host, key )

        def write[L <: HList]( arguments: L )( implicit lf: LeftFolder.Aux[L, Intent, intent.fold.type, Intent] ) = {
            arguments.foldLeft( host )( intent.fold )
        }
    }
}