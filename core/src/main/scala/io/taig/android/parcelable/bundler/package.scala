package io.taig.android.parcelable

import shapeless.labelled.FieldType
import shapeless.{ Lazy, Poly2, Witness }

package object bundler {
    import codecs._

    object fold extends Poly2 {
        implicit def default[K <: Symbol, V](
            implicit
            k: Witness.Aux[K],
            e: Lazy[bundle.Encoder[V]]
        ): Case.Aux[Bundle, FieldType[K, V], Bundle] = at[Bundle, FieldType[K, V]] { ( bundle, value ) ⇒
            bundle.write[V]( k.value.name, value )( e.value )
            bundle
        }
    }
}