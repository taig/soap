package io.taig.android.parcelable

import shapeless._
import shapeless.labelled._
import shapeless.ops.hlist.LeftFolder

object Bundle {
    def empty = android.os.Bundle.EMPTY

    def apply( capacity: Int ): Bundle = new Bundle( capacity )

    def apply[T: Bundleize.Write]( key: String, value: T ): Bundle = Bundle( 1 ).write( key, value )

    def apply[H <: HList]( arguments: H )(
        implicit
        lf: LeftFolder.Aux[H, Bundle, fold.write.type, Bundle]
    ): Bundle = {
        arguments.foldLeft( Bundle( arguments.runtimeLength ) )( fold.write )
    }

    private object fold {
        object write extends Poly2 {
            implicit def string[K, V: Bundleize.Write]( implicit w: Witness.Aux[K] ) = {
                at[Bundle, FieldType[K, V]] { ( bundle, value ) ⇒
                    val key = w.value match {
                        case symbol: Symbol ⇒ symbol.name
                        case string: String ⇒ string
                    }

                    implicitly[Bundleize.Write[V]].write( bundle, key, value )

                    bundle
                }
            }
        }
    }
}