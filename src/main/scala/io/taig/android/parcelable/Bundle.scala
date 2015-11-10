package io.taig.android.parcelable

import shapeless._
import shapeless.labelled._
import shapeless.ops.hlist.LeftFolder

object Bundle {
    def empty = android.os.Bundle.EMPTY

    def apply( capacity: Int ): Bundle = new Bundle( capacity )

    def apply[H <: HList]( arguments: H )(
        implicit
        lf: LeftFolder.Aux[H, Bundle, fold.write.type, Bundle]
    ): Bundle = {
        arguments.foldLeft( Bundle( arguments.runtimeLength ) )( fold.write )
    }

    private object fold {
        object write extends Poly2 {
            implicit def string[K <: String, V: Bundleize.Write]( implicit key: Witness.Aux[K] ) = {
                at[Bundle, FieldType[K, V]] { ( bundle, value ) ⇒
                    implicitly[Bundleize.Write[V]].write( bundle, key.value, value )
                    bundle
                }
            }

            implicit def symbol[K <: Symbol, V: Bundleize.Write]( implicit key: Witness.Aux[K] ) = {
                at[Bundle, FieldType[K, V]] { ( bundle, value ) ⇒
                    implicitly[Bundleize.Write[V]].write( bundle, key.value.name, value )
                    bundle
                }
            }
        }
    }
}