package io.taig.android

import shapeless.HList
import shapeless.ops.hlist.LeftFolder

import scala.language.higherKinds

package object parcelable {
    type Bundle = android.os.Bundle

    trait ParcelableCodec[H] {
        protected def host: H
    }

    abstract class ParcelableEncoder[H, I[V], E[V] <: Encoder.Aux[I[V], _]] extends ParcelableCodec[H] {
        protected def encode[V]( key: String, value: V ): E[V]#Input

        def write[V: E]( key: String, value: V ): H = {
            implicitly[E[V]].encode( encode[V]( key, value ) )
            host
        }
    }

    trait ParcelableDecoder[H, I, D[T] <: Decoder.Aux[I, T]] extends ParcelableCodec[H] {
        protected def decode[T]( key: String ): D[T]#Input

        def read[T: D]( key: String ): T = implicitly[D[T]].decode( decode( key ) )

        def write[L <: HList]( arguments: L )( implicit lf: LeftFolder.Aux[L, H, bundle.fold.type, H] ) = {
            arguments.foldLeft( host )( bundle.fold )
        }
    }

    implicit class ParcelableBundle( val host: Bundle )
            extends ParcelableEncoder[Bundle, ( { type λ[ɣ] = ( Bundle, String, ɣ ) } )#λ, bundle.Encoder]
            with ParcelableDecoder[Bundle, ( Bundle, String ), bundle.Decoder] {
        override protected def encode[T]( key: String, value: T ) = ( host, key, value )

        override protected def decode[T]( key: String ) = ( host, key )
    }
}