package io.taig.android.soap.ops

import io.taig.android.soap._
import shapeless.HList

class writer[C]( container: C ) {
    def write[V]( key: String, value: V )( implicit w: Writer[C, V] ): C = {
        w.write( container, key, value )
        container
    }

    def write[L <: HList]( arguments: L )( implicit lf: fold.F[L, C] ): C = {
        arguments.foldLeft( container )( fold )
    }
}