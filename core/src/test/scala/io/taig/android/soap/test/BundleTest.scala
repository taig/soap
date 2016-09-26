package io.taig.android.soap.test

import android.os.Build.VERSION_CODES._
import io.circe.syntax._
import io.taig.android.soap.Bundle
import io.taig.android.soap.syntax.reader._
import io.taig.android.soap.syntax.writer._
import org.robolectric.annotation.Config
import shapeless.syntax.singleton._
import shapeless._

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

    it should "have a capacity constructor" in {
        Bundle( 10 ) shouldBe Bundle.empty
    }

    it should "have a single element constructor" in {
        Bundle( "foo", "bar" ) shouldBe Bundle( 2 ).write( "foo", "bar" )
    }

    it should "have a record constructor" in {
        Bundle( "foo" ->> "bar" :: "foobar" ->> 42 :: HNil ) shouldBe
            Bundle( 2 ).write( "foo", "bar" ).write( "foobar", 42 )
    }
}