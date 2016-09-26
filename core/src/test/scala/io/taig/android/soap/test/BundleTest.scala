package io.taig.android.soap.test

import android.os.Build.VERSION_CODES._
import io.circe.syntax._
import io.taig.android.soap.Bundle
import io.taig.android.soap.syntax.reader._
import io.taig.android.soap.syntax.writer._
import org.robolectric.annotation.Config

@Config( sdk = Array( LOLLIPOP ) )
class BundleTest extends Suite {
    it should "have a write method" in {
        val bundle = Bundle( 1 )
        bundle.write( "key", "value" )
        bundle.getString( "key" ) shouldBe "\"value\""
    }

    it should "have a read method" in {
        val bundle = Bundle( 1 )
        bundle.putString( "key", "value".asJson.noSpaces )
        bundle.read[String]( "key" ) shouldBe Some( "value" )
        bundle.read[String]( "foobar" ) shouldBe None
    }
}