package io.taig.android.parcelable

import shapeless.labelled.FieldType
import shapeless.{ Poly2, Witness }

package object bundler {
    private[bundler] object fold extends Poly2 {
        implicit def default[K <: Symbol, V: bundle.Encoder](
            implicit
            key: Witness.Aux[K]
        ): Case.Aux[Bundle, FieldType[K, V], Bundle] = {
            at[Bundle, FieldType[K, V]] { ( bundle, value ) ⇒
                bundle.write[V]( key.value.name, value )
                bundle
            }
        }
    }
}