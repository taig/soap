package io.taig.android.parcelable

import android.content.Intent
import shapeless._
import shapeless.labelled._
import shapeless.ops.hlist.LeftFolder

import scala.language.higherKinds

package object bundleable {
    type Bundle = android.os.Bundle

    trait ParcelableCodec[H] {
        def host: H

        def read[T]( key: String )( implicit c: Codec[H, T] ): T = c.safeDecode( host, key )

        def write[T]( key: String, value: T )( implicit c: Codec[H, T] ): H = {
            c.encode( host, key, value )
            host
        }

        def write[L <: HList]( arguments: L )( implicit lf: LeftFolder.Aux[L, H, fold.type, H] ) = {
            arguments.foldLeft( host )( fold )
        }
    }

    implicit class ParcelableBundle( val host: Bundle ) extends ParcelableCodec[Bundle]

    implicit class ParcelableIntent( val host: Intent ) extends ParcelableCodec[Intent]

    private[bundleable] object fold extends Poly2 {
        implicit def default[K, V, H]( implicit w: Witness.Aux[K], c: Codec[H, V] ) = {
            at[H, FieldType[K, V]] { ( host, value ) ⇒
                val key = w.value match {
                    case symbol: Symbol ⇒ symbol.name
                    case string: String ⇒ string
                }

                c.encode( host, key, value )
                host
            }
        }
    }
}