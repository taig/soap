package io.taig.android

import _root_.android.os.Bundle

package object parcelable {
    implicit class RichBundle( bundle: Bundle ) {
        def read[T: Bundleize]( key: String ) = implicitly[Bundleize[T]].read( key, bundle )

        def write[T: Bundleize]( key: String, value: T ) = implicitly[Bundleize[T]].write( key, value, bundle )
    }
}