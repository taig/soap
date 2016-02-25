package io.taig.android.soap.ops

import android.content.Intent
import io.taig.android.soap.intent._
import shapeless.HList
import shapeless.ops.hlist.LeftFolder

class intent( intent: Intent ) {
    def write[V: Encoder]( key: String, value: V ): Intent = {
        Encoder[V].encode( ( intent, key, value ) )
        intent
    }

    def write[L <: HList]( arguments: L )(
        implicit
        lf: LeftFolder.Aux[L, Intent, fold.type, Intent]
    ): Intent = arguments.foldLeft( intent )( fold )

    def read[V: Decoder]( key: String ): V = Decoder[V].decode( ( intent, key ) )
}