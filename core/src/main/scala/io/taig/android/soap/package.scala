package io.taig.android

import shapeless.labelled._
import shapeless.ops.hlist.LeftFolder
import shapeless.{ HList, Lazy, Poly2, Witness }

package object soap {
    type Bundle = android.os.Bundle

    object fold extends Poly2 {
        type F[L <: HList, C] = LeftFolder.Aux[L, C, this.type, C]

        implicit def default[C, K, V](
            implicit
            k: Witness.Aux[K],
            w: Lazy[Writer[C, V]]
        ): Case.Aux[C, FieldType[K, V], C] = at { ( container, value ) ⇒
            val key = k.value match {
                case symbol: Symbol ⇒ symbol.name
                case any            ⇒ any.toString
            }

            w.value.write( container, key, value )
            container
        }
    }
}