package io.taig

import _root_.android.os.Bundle
import io.taig.android.parcelable.Bundleize

package object android {
    implicit class RichBundle( bundle: Bundle ) {
        def get[T: Bundleize]( key: String ) = implicitly[Bundleize[T]].get( key, bundle )

        def put[T: Bundleize]( key: String, value: T ) = implicitly[Bundleize[T]].put( key, value, bundle )
    }
}