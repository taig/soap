package io.taig.android.soap.test

import android.net.Uri
import android.os.Build.VERSION_CODES._
import android.preference.PreferenceManager
import io.circe.syntax._
import io.taig.android.soap.implicits._
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.scalatest.BeforeAndAfterEach

@Config( sdk = Array( LOLLIPOP ) )
class SharedPreferencesTest
        extends Suite
        with BeforeAndAfterEach {
    lazy val preferences = PreferenceManager
        .getDefaultSharedPreferences( RuntimeEnvironment.application )

    override def afterEach(): Unit = {
        super.afterEach()
        preferences.edit().clear().commit()
    }

    it should "have a write method" in {
        preferences.write( "key", "value" )
        preferences.getString( "key", null ) shouldBe "\"value\""
    }

    it should "have a read method" in {
        preferences
            .edit()
            .putString( "key", "value".asJson.noSpaces )
            .commit()

        preferences.read[String]( "key" ) shouldBe Some( "value" )
        preferences.read[String]( "foobar" ) shouldBe None
    }

    it should "support Parcelable" in {
        preferences.write( "key", Uri.parse( "http://taig.io/" ) )
        preferences.read[Uri]( "key" ) shouldBe
            Some( Uri.parse( "http://taig.io/" ) )
    }
}