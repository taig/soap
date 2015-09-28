package io.taig.android

import _root_.android.os.Bundle
import android.content.Intent

package object parcelable {
    implicit class ParcelableBundle( bundle: Bundle ) {
        def read[T: Bundleize]( key: String ) = implicitly[Bundleize[T]].read( key, bundle )

        def write[T: Bundleize]( key: String, value: T ) = implicitly[Bundleize[T]].write( key, value, bundle )
    }

    implicit class ParcelableIntent( intent: Intent ) {
        def read[T: Bundleize]( key: String ) = intent.getExtras.read[T]( key )

        def write[T: Bundleize]( key: String, value: T ) = {
            if ( intent.getExtras == null ) {
                intent.putExtras( new Bundle() )
            }

            intent.getExtras.write( key, value )
        }
    }
}