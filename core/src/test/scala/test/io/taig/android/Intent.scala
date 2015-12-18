package test.io.taig.android

import java.net.URL

import android.content.{ Intent ⇒ AIntent }
import android.net.Uri
import android.os.Build.VERSION_CODES._
import android.os.Parcelable
import io.taig.android.parcelable.bundle.codecs._
import io.taig.android.parcelable.intent.{ Decoder, Encoder }
import io.taig.android.parcelable.intent.codecs._
import io.taig.android.parcelable.{ Bundle ⇒ ABundle, _ }
import org.robolectric.annotation.Config
import shapeless._
import shapeless.syntax.singleton._

@Config( sdk = Array( LOLLIPOP ) )
class Intent extends Suite {
    def verify[V: Encoder: Decoder]( value: V ) = {
        val intent = new AIntent()
        intent.write[V]( "value", value )
        intent.read[V]( "value" ) shouldEqual value
        intent
    }

    it should "support Array" in {
        verify[Array[Boolean]]( Array( true, false ) )
        verify[Array[Byte]]( Array( Byte.MaxValue, Byte.MinValue ) )
        verify[Array[Char]]( Array( Char.MaxValue, Char.MinValue ) )
        verify[Array[Double]]( Array( Double.MaxValue, Double.MinValue ) )
        verify[Array[Float]]( Array( Float.MaxValue, Float.MinValue ) )
        verify[Array[Int]]( Array( Int.MaxValue, Int.MinValue ) )
        verify[Array[Long]]( Array( Long.MaxValue, Long.MinValue ) )
        verify[Array[Short]]( Array( Short.MaxValue, Short.MinValue ) )
        verify[Array[String]]( Array( "1", "2", "3" ) )
        verify[Array[Uri]]( Array( Uri.parse( "http://taig.io/" ) ) )
        verify[Array[Animal]]( Array( Dog( "Hoschi" ) ) )
    }

    it should "support Boolean" in {
        verify[Boolean]( true )
        verify[Boolean]( false )
    }

    it should "support Bundle" in {
        verify[ABundle]( ABundle( "int" ->> 3 :: "string" ->> "asdf" :: "long" ->> 5l :: HNil ) )
        verify[ABundle]( ABundle( 'int ->> 3 :: 'string ->> "asdf" :: 'long ->> 5l :: HNil ) )
    }

    it should "support Bundler" in {
        verify[Cat]( Cat( true ) )
    }

    it should "support Byte" in {
        verify[Byte]( 3.toByte )
    }

    it should "support Char" in {
        verify[Char]( 'c' )
    }

    it should "support CharSequence" in {
        verify[CharSequence]( "asdf" )
    }

    it should "support Double" in {
        verify[Double]( 3.14d )
    }

    it should "support Float" in {
        verify[Float]( 3.14f )
    }

    it should "support Int" in {
        verify[Int]( 3 )
    }

    it should "support Long" in {
        verify[Long]( 3l )
    }

    it should "support Option" in {
        verify[Option[Int]]( Option( 3 ) )
        verify[Option[Int]]( None )
    }

    it should "support Option with null values" in {
        new AIntent().write[String]( "value", null ).read[Option[String]]( "value" ) shouldEqual None
    }

    it should "support Parcelable" in {
        verify[Parcelable]( Uri.parse( "http://taig.io/" ) )
        verify[Uri]( Uri.parse( "http://taig.io/" ) )
    }

    it should "support sealed trait enums" in {
        verify[Enum]( Enum.B ).getExtras shouldEqual ABundle( "value", "B" )
    }

    it should "support Short" in {
        verify[Short]( 3.toShort )
    }

    it should "support String" in {
        verify[String]( "asdf" )
    }

    it should "support Traversable" in {
        verify[Traversable[Float]]( Seq( 3.4f, 1.2f, 6.7f ) )
        verify[Seq[Float]]( Seq( 3.4f, 1.2f, 6.7f ) )
        verify[Vector[Uri]]( Vector( Uri.parse( "http://taig.io/" ) ) )
        verify[List[Animal]]( List( Dog( "Hoschi" ) ) )
    }

    it should "support URL" in {
        verify[URL]( new URL( "http://taig.io/" ) )
    }

    it should "fail when accessing a non-existing value" in {
        intercept[exception.KeyNotFound] {
            new AIntent().read[Int]( "a" )
        }
    }
}