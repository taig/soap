package io.taig.android.soap.test

import android.os.Build.VERSION_CODES._
import android.preference.PreferenceManager
import io.circe.syntax._
import io.taig.android.soap.syntax.reader._
import io.taig.android.soap.syntax.writer._
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.scalatest.BeforeAndAfterEach
import shapeless.syntax.singleton._
import shapeless._

@Config( sdk = Array( LOLLIPOP ) )
class SharedPreferencesTest
        extends Suite
        with BeforeAndAfterEach {
    lazy val preferences = PreferenceManager
        .getDefaultSharedPreferences( RuntimeEnvironment.application )

    override protected def afterEach(): Unit = {
        super.afterEach()
        preferences.edit().clear().commit()
    }

    it should "have a write method" in {
        preferences.write( "key", "value" )
        preferences.getString( "key", null ) shouldBe "\"value\""
    }

    it should "have a writeAll method" in {
        preferences.writeAll( 'key ->> "value" :: "foobar" ->> 42 :: HNil )
        preferences.read[String]( "key" ) shouldBe Some( "value" )
        preferences.read[Int]( "foobar" ) shouldBe Some( 42 )
    }

    it should "have a read method" in {
        preferences
            .edit()
            .putString( "key", "value".asJson.noSpaces )
            .commit()

        preferences.read[String]( "key" ) shouldBe Some( "value" )
        preferences.read[String]( "foobar" ) shouldBe None
    }
}