package io.taig.android.soap.test

import android.content.Intent
import android.os.Build.VERSION_CODES._
import io.circe.syntax._
import io.taig.android.soap.syntax.reader._
import io.taig.android.soap.syntax.writer._
import org.robolectric.annotation.Config

@Config( sdk = Array( LOLLIPOP ) )
class IntentTest extends Suite {
    it should "have a write method" in {
        val intent = new Intent()
        intent.write( "key", "value" )
        intent.getStringExtra( "key" ) shouldBe "\"value\""
    }

    it should "have a read method" in {
        val intent = new Intent()
        intent.putExtra( "key", "value".asJson.noSpaces )
        intent.read[String]( "key" ) shouldBe Some( "value" )
        intent.read[String]( "foobar" ) shouldBe None
    }
}