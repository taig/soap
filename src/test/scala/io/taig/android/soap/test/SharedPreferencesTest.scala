package io.taig.android.soap.test

import android.content.{ Intent, SharedPreferences }
import android.os.Build.VERSION_CODES._
import android.preference.PreferenceManager
import io.circe.syntax._
import io.taig.android.soap.syntax.reader._
import io.taig.android.soap.syntax.writer._
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@Config( sdk = Array( LOLLIPOP ) )
class SharedPreferencesTest extends Suite {
    lazy val preferences = PreferenceManager
        .getDefaultSharedPreferences( RuntimeEnvironment.application )

    it should "have a write method" in {
        preferences.write( "key", "value" )
        preferences.getString( "key", null ) shouldBe "\"value\""
    }

    it should "have a read method" in {
        preferences
            .edit()
            .putString( "key2", "value".asJson.noSpaces )
            .commit()

        preferences.read[String]( "key2" ) shouldBe Some( "value" )
        preferences.read[String]( "foobar" ) shouldBe None
    }
}