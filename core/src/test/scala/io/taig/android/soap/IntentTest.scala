package io.taig.android.soap

import android.content.Intent
import android.os.Build.VERSION_CODES._
import io.taig.android.soap.generic.auto._
import io.taig.android.soap.Reader.readerIntentSerializable
import io.taig.android.soap.Writer.writerIntentSerializable
import io.taig.android.soap.syntax.reader._
import io.taig.android.soap.syntax.writer._
import org.robolectric.annotation.Config
import shapeless._
import shapeless.syntax.singleton._

@Config( sdk = Array( LOLLIPOP ) )
class IntentTest extends WriterReaderTest[Intent](
    writerIntentSerializable,
    readerIntentSerializable
) {
    override def instance = new Intent()

    it should "have a write method" in {
        val intent = new Intent()
        intent.write( "key", "value" )
        intent.getStringExtra( "key" ) shouldBe "value"

        intent.write( "foo" ->> "bar" :: "lorem" ->> "ipsum" :: HNil )
        intent.getStringExtra( "foo" ) shouldBe "bar"
        intent.getStringExtra( "lorem" ) shouldBe "ipsum"
    }

    it should "have a read method" in {
        val intent = new Intent()
        intent.putExtra( "key", "value" )
        intent.read[String]( "key" ) shouldBe Some( "value" )
    }
}