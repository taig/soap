package io.taig.android.parcelable.bundleable

import android.content.{ Intent ⇒ AIntent }
import android.os.{ Bundle ⇒ ABundle }

trait Codec[H, T] {
    def encode( host: H, key: String, value: T ): Unit

    def decode( host: H, key: String ): T

    def safeDecode( host: H, key: String ): T = {
        if ( contains( host, key ) ) {
            decode( host, key )
        } else {
            throw ??? //exception.KeyNotFound( key )
        }
    }

    /**
     * Verify whether a host contains a given key
     */
    def contains( host: H, key: String ): Boolean
}

object Codec {
    /**
     * Type class that describes how to store/extract a type T to/from a given Bundle
     */
    trait Bundle[T] extends Codec[ABundle, T] {
        override def contains( bundle: ABundle, key: String ) = bundle.containsKey( key )
    }

    object Bundle extends BundleCodecs with Codecs[ABundle] {
        def apply[T]( e: ( ABundle, String, T ) ⇒ Unit, d: ( ABundle, String ) ⇒ T ) = new Bundle[T] {
            override def encode( bundle: ABundle, key: String, value: T ) = e( bundle, key, value )

            override def decode( bundle: ABundle, key: String ) = d( bundle, key )
        }
    }

    /**
     * Type class that describes how to store/extract a type T to/from an Intent
     *
     * An Intent wraps the Bundle API with custom put/get methods and provides no clean way to interopt with the underlying
     * Bundle directly. Therefore, instances of this type class contain knowledge of how to interact with the wrapper API
     * in order to avoid wrapping every value in a costly Bundle, only falling back to Bundler when strictly necessary.
     */
    trait Intent[T] extends Codec[AIntent, T] {
        override def contains( intent: AIntent, key: String ) = intent.hasExtra( key )
    }

    object Intent extends IntentCodecs with Codecs[AIntent] {
        def apply[T]( e: ( AIntent, String, T ) ⇒ Unit, d: ( AIntent, String ) ⇒ T ) = new Intent[T] {
            override def encode( intent: AIntent, key: String, value: T ) = e( intent, key, value )

            override def decode( intent: AIntent, key: String ) = d( intent, key )
        }
    }
}