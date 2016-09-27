package io.taig.android.soap.test

import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION_CODES._
import io.circe.syntax._
import io.taig.android.soap.Bundle
import io.taig.android.soap.implicits._
import org.robolectric.annotation.Config
import shapeless.syntax.singleton._
import shapeless._

@Config( sdk = Array( LOLLIPOP ) )
class IntentTest extends Suite {
    it should "have a write method" in {
        val intent = new Intent()
        intent.write( "key", "value" )
        intent.getStringExtra( "key" ) shouldBe "\"value\""
    }

    it should "have a writeAll method" in {
        val intent1 = new Intent()
        val intent2 = new Intent()
        intent1.writeAll( 'key ->> "value" :: "foobar" ->> 42 :: HNil ) shouldBe
            intent2.write( "key", "value" ).write( "foobar", 42 )
    }

    it should "have a read method" in {
        val intent = new Intent()
        intent.putExtra( "key", "value".asJson.noSpaces )
        intent.read[String]( "key" ) shouldBe Some( "value" )
        intent.read[String]( "foobar" ) shouldBe None
    }

    it should "support Parcelable" in {
        val intent = new Intent()
        intent.write( "key", Uri.parse( "http://taig.io/" ) )
        intent.read[Uri]( "key" ) shouldBe
            Some( Uri.parse( "http://taig.io/" ) )
    }
}