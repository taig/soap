package io.taig.android

import _root_.android.os.Bundle

package object parcelable {
    implicit class RichBundle( bundle: Bundle ) {
        def get[T: Bundleize]( key: String ) = implicitly[Bundleize[T]].get( key, bundle )

        def put[T: Bundleize]( key: String, value: T ) = implicitly[Bundleize[T]].put( key, value, bundle )
    }
}