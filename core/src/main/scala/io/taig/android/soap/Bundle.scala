package io.taig.android.soap

import io.taig.android.soap.syntax.writer._
import shapeless.HList

object Bundle {
    def empty = android.os.Bundle.EMPTY

    def apply( capacity: Int ): Bundle = new Bundle( capacity )

    def apply[V: Writer.Bundle]( key: String, value: V ): Bundle = Bundle( 1 ).write( key, value )

    def apply[L <: HList]( arguments: L )( implicit lf: fold.F[L, Bundle] ): Bundle = {
        arguments.foldLeft( Bundle( arguments.runtimeLength ) )( fold )
    }
}