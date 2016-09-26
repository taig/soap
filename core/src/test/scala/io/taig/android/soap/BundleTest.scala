package io.taig.android.soap

import android.os.Build.VERSION_CODES._
import io.taig.android.soap.generic.auto._
import io.taig.android.soap.Reader.readerBundleSerializable
import io.taig.android.soap.Writer.writerBundleSerializable
import io.taig.android.soap.syntax.reader._
import io.taig.android.soap.syntax.writer._
import org.robolectric.annotation.Config
import shapeless._
import shapeless.syntax.singleton._

@Config( sdk = Array( LOLLIPOP ) )
class BundleTest extends WriterReaderTest[Bundle](
    writerBundleSerializable,
    readerBundleSerializable
) {
    override def instance = Bundle( 1 )

    it should "have a write method" in {
        val bundle = Bundle( 3 )
        bundle.write( "key", "value" )
        bundle.getString( "key" ) shouldBe "value"

        bundle.write( "foo" ->> "bar" :: "lorem" ->> "ipsum" :: HNil )
        bundle.getString( "foo" ) shouldBe "bar"
        bundle.getString( "lorem" ) shouldBe "ipsum"
    }

    it should "have a read method" in {
        val bundle = Bundle( 1 )
        bundle.putString( "key", "value" )
        bundle.read[String]( "key" ) shouldBe Some( "value" )
    }
}