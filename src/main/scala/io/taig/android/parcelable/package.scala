package io.taig.android

import android.os.Bundle
import android.content.Intent

package object parcelable {
    implicit class ParcelableBundle( val bundle: Bundle ) {
        def read[T: Bundleize]( key: String ) = implicitly[Bundleize[T]].read( key, bundle )

        def write[T: Bundleize]( key: String, value: T ): Bundle = {
            implicitly[Bundleize[T]].write( key, value, bundle )
            bundle
        }
    }

    implicit class ParcelableIntent( val intent: Intent ) {
        val field = classOf[Intent].getDeclaredField( "mExtras" )
        field.setAccessible( true )
        var bundle = field.get( intent ).asInstanceOf[Bundle]

        def read[T: Bundleize]( key: String ) = bundle.read[T]( key )

        def write[T: Bundleize]( key: String, value: T ): Intent = {
            if ( bundle == null ) {
                bundle = new Bundle()
                field.set( intent, bundle )
            }

            bundle.write( key, value )
            intent
        }
    }
}