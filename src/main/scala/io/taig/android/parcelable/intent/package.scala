package io.taig.android.parcelable

import android.content.Intent
import shapeless.labelled._
import shapeless.{ Lazy, Poly2, Witness }

package object intent {
    object fold extends Poly2 {
        implicit def default[K, V](
            implicit
            k: Witness.Aux[K],
            e: Lazy[Encoder[V]]
        ): Case.Aux[Intent, FieldType[K, V], Intent] = {
            at[Intent, FieldType[K, V]] { ( intent, value ) ⇒
                val key = k.value match {
                    case symbol: Symbol ⇒ symbol.name
                    case string: String ⇒ string
                }

                e.value.encode( ( intent, key, value ) )
                intent
            }
        }
    }
}