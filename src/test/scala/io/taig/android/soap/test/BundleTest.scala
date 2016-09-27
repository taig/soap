package io.taig.android.soap.test

import android.net.Uri
import android.os.Build.VERSION_CODES._
import android.util.{ Size, SizeF }
import io.circe.syntax._
import io.taig.android.soap.Bundle
import io.taig.android.soap.implicits._
import org.robolectric.annotation.Config

@Config( sdk = Array( LOLLIPOP ) )
class BundleTest extends Suite {
    it should "have a capacity constructor" in {
        Bundle( 10 ) shouldBe Bundle.empty
    }

    it should "have a single element constructor" in {
        Bundle( "foo", "bar" ) shouldBe Bundle( 2 ).write( "foo", "bar" )
    }

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

    it should "support Parcelable" in {
        val bundle = Bundle( 1 )
        bundle.write( "key", Uri.parse( "http://taig.io/" ) )
        bundle.read[Uri]( "key" ) shouldBe
            Some( Uri.parse( "http://taig.io/" ) )
    }

    it should "support Size" in {
        val bundle = Bundle( 1 )
        bundle.write( "key", new Size( 12, 34 ) )
        bundle.read[Size]( "key" ) shouldBe
            Some( new Size( 12, 34 ) )
    }

    it should "support SizeF" in {
        val bundle = Bundle( 1 )
        bundle.write( "key", new SizeF( 12.3f, 45.6f ) )
        bundle.read[SizeF]( "key" ) shouldBe
            Some( new SizeF( 12.3f, 45.6f ) )
    }
}