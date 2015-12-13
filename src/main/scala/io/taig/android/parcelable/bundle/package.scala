package io.taig.android.parcelable

import shapeless.labelled._
import shapeless.{ Poly2, Witness }

package object bundle {
    object fold extends Poly2 {
        implicit def default[K, V](
            implicit
            k: Witness.Aux[K],
            e: Encoder[V]
        ): Case.Aux[Bundle, FieldType[K, V], Bundle] = {
            at[Bundle, FieldType[K, V]] { ( host, value ) ⇒
                val key = k.value match {
                    case symbol: Symbol ⇒ symbol.name
                    case string: String ⇒ string
                }

                e.encode( ( host, key, value ) )
                host
            }
        }
    }
}