package io.taig.android

import android.content.Intent
import android.os.Parcel

package object parcelable {
    type Bundle = android.os.Bundle

    implicit class ParcelableBundle( val bundle: Bundle ) {
        private[parcelable] def checked[T]( key: String )( f: ( Bundle, String ) ⇒ T ): T = {
            if ( bundle.containsKey( key ) ) {
                f( bundle, key )
            } else {
                throw new IllegalStateException( s"Bundle does not contain key '$key'" )
            }
        }

        def read[T: Bundleize.Read]( key: String ) = implicitly[Bundleize.Read[T]].read( bundle, key )

        def write[T: Bundleize.Write]( key: String, value: T ): Bundle = {
            implicitly[Bundleize.Write[T]].write( bundle, key, value )
            bundle
        }
    }

    implicit class ParcelableIntent( val intent: Intent ) {
        val field = classOf[Intent].getDeclaredField( "mExtras" )
        field.setAccessible( true )
        var bundle = field.get( intent ).asInstanceOf[Bundle]

        def read[T: Bundleize.Read]( key: String ) = bundle.read[T]( key )

        def write[T: Bundleize.Write]( key: String, value: T ): Intent = {
            if ( bundle == null ) {
                bundle = new Bundle()
                field.set( intent, bundle )
            }

            bundle.write( key, value )
            intent
        }
    }

    implicit class ParcelableParcel( val parcel: Parcel ) {
        def read[T: Parcelize] = implicitly[Parcelize[T]].read( parcel )

        def write[T: Parcelize]( value: T, flags: Int = 0 ) = implicitly[Parcelize[T]].write( value, parcel, flags )
    }
}