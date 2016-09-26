package io.taig.android.soap

import io.circe.Encoder
import io.taig.android.soap.syntax.writer._

object Bundle {
    val empty = android.os.Bundle.EMPTY

    def apply( capacity: Int ): Bundle = new Bundle( capacity )

    def apply[V: Encoder]( key: String, value: V ): Bundle = {
        Bundle( 1 ).write( key, value )
    }

    //    def apply[L <: HList]( arguments: L )( implicit lf: fold.F[L, Bundle] ): Bundle = {
    //        arguments.foldLeft( Bundle( arguments.runtimeLength ) )( fold )
    //    }
}