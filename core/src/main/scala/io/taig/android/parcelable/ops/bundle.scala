package io.taig.android.parcelable.ops

import io.taig.android.parcelable.Bundle
import io.taig.android.parcelable.bundle._
import shapeless.HList
import shapeless.ops.hlist.LeftFolder

class bundle( bundle: Bundle ) {
    def write[V: Encoder]( key: String, value: V ): Bundle = {
        Encoder[V].encode( ( bundle, key, value ) )
        bundle
    }

    def write[L <: HList]( arguments: L )(
        implicit
        lf: LeftFolder.Aux[L, Bundle, fold.type, Bundle]
    ): Bundle = arguments.foldLeft( bundle )( fold )

    def read[V: Decoder]( key: String ): V = Decoder[V].decode( ( bundle, key ) )
}