package io.taig.android

import android.content.Intent
import shapeless.HList
import shapeless.ops.hlist.LeftFolder

import scala.language.higherKinds

package object parcelable {
    type Bundle = android.os.Bundle

    implicit class ParcelableBundle( b: Bundle ) {
        def read[V: bundle.Codec]( key: String ): V = bundle.Codec[V].decode( ( b, key ) )

        def write[V: bundle.Codec]( key: String, value: V ): Bundle = {
            bundle.Codec[V].encode( ( b, key, value ) )
            b
        }

        def write[L <: HList]( arguments: L )(
            implicit
            lf: LeftFolder.Aux[L, Bundle, bundle.fold.type, Bundle]
        ): Bundle = arguments.foldLeft( b )( bundle.fold )
    }

    implicit class ParcelableIntent( i: Intent ) {
        def read[V: intent.Codec]( key: String ): V = intent.Codec[V].decode( ( i, key ) )

        def write[V: intent.Codec]( key: String, value: V ): Intent = {
            intent.Codec[V].encode( ( i, key, value ) )
            i
        }

        def write[L <: HList]( arguments: L )(
            implicit
            lf: LeftFolder.Aux[L, Intent, intent.fold.type, Intent]
        ): Intent = arguments.foldLeft( i )( intent.fold )
    }
}