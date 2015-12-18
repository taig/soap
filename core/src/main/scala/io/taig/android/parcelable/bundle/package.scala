package io.taig.android.parcelable

import shapeless.labelled._
import shapeless.{ Lazy, Poly2, Witness }

package object bundle {
    import codecs._

    object fold extends Poly2 {
        implicit def default[K, V](
            implicit
            k: Witness.Aux[K],
            c: Lazy[bundle.Encoder[V]]
        ): Case.Aux[Bundle, FieldType[K, V], Bundle] = at[Bundle, FieldType[K, V]] { ( bundle, value ) ⇒
            val key = k.value match {
                case symbol: Symbol ⇒ symbol.name
                case string: String ⇒ string
            }

            c.value.encode( ( bundle, key, value ) )
            bundle
        }
    }
}