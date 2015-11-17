package io.taig.android

import android.content.Intent
import android.os.Bundle

package object parcelable {
    implicit class ParcelableBundle( val bundle: Bundle ) {
        def read[T: Bundleize]( key: String ) = {
            implicitly[Bundleize[T]].read( key, bundle )
        }

        def write[T: Bundleize]( key: String, value: T ): Bundle = {
            implicitly[Bundleize[T]].write( key, value, bundle )
            bundle
        }
    }

    implicit class ParcelableIntent( val intent: Intent ) {
        def read[T: Bundleize]( key: String ) = intent.getBundleExtra( key ).read[T]( "value" )

        def write[T: Bundleize]( key: String, value: T ): Intent = {
            val bundle = new Bundle( 1 )
            intent.putExtra( key, bundle.write( "value", value ) )
            intent
        }
    }
}