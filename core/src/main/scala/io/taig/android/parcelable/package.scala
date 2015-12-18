package io.taig.android

import android.content.Intent
import shapeless.HList
import shapeless.ops.hlist.LeftFolder

import scala.language.higherKinds

package object parcelable {
    type Bundle = android.os.Bundle

    implicit class ParcelableBundle( b: Bundle ) {
        def write[V: bundle.Encoder]( key: String, value: V ): Bundle = {
            bundle.Encoder[V].encode( ( b, key, value ) )
            b
        }

        def write[L <: HList]( arguments: L )(
            implicit
            lf: LeftFolder.Aux[L, Bundle, bundle.fold.type, Bundle]
        ): Bundle = arguments.foldLeft( b )( bundle.fold )

        def read[V: bundle.Decoder]( key: String ): V = bundle.Decoder[V].decode( ( b, key ) )
    }

    implicit class ParcelableIntent( i: Intent ) {
        def write[V: intent.Encoder]( key: String, value: V ): Intent = {
            intent.Encoder[V].encode( ( i, key, value ) )
            i
        }

        def write[L <: HList]( arguments: L )(
            implicit
            lf: LeftFolder.Aux[L, Intent, intent.fold.type, Intent]
        ): Intent = arguments.foldLeft( i )( intent.fold )

        def read[V: intent.Decoder]( key: String ): V = intent.Decoder[V].decode( ( i, key ) )
    }
}