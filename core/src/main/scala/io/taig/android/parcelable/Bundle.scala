package io.taig.android.parcelable

import shapeless.HList
import shapeless.ops.hlist.LeftFolder

object Bundle {
    def empty = android.os.Bundle.EMPTY

    def apply( capacity: Int ): Bundle = new Bundle( capacity )

    def apply[V: bundle.Codec]( key: String, value: V ): Bundle = Bundle( 1 ).write( key, value )

    def apply[L <: HList]( arguments: L )( implicit lf: LeftFolder.Aux[L, Bundle, bundle.fold.type, Bundle] ): Bundle = {
        arguments.foldLeft( Bundle( arguments.runtimeLength ) )( bundle.fold )
    }
}