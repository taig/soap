package test.io.taig.android

import java.net.URL

import android.content.{ Intent ⇒ AIntent }
import android.net.Uri
import android.os.Build.VERSION_CODES._
import android.os.Parcelable
import io.taig.android.parcelable.intent.{ Decoder, Encoder }
import io.taig.android.parcelable.{ Bundle ⇒ ABundle, _ }
import org.robolectric.annotation.Config
import shapeless._
import shapeless.syntax.singleton._

@Config( sdk = Array( LOLLIPOP ) )
class Intent extends Suite {
    def symmetric[T: Encoder: Decoder]( value: T ) = asymmetric[T, T]( value, value )

    def asymmetric[I: Encoder, O: Decoder]( value: I, result: O ) = {
        val intent = new AIntent()
        intent.write( "value", value )
        intent.read[O]( "value" ) shouldEqual result
        intent
    }

    it should "support Array" in {
        symmetric[Array[Int]]( Array( 1, 2, 3 ) )
        symmetric[Array[String]]( Array( "1", "2", "3" ) )
        symmetric[Array[Uri]]( Array( Uri.parse( "http://taig.io/" ) ) )
        symmetric[Array[Animal]]( Array( Dog( "Hoschi" ) ) )
    }

    it should "support Boolean" in {
        symmetric[Boolean]( true )
        symmetric[Boolean]( false )
    }

    it should "support Bundle" in {
        symmetric[ABundle]( ABundle( "int" ->> 3 :: "string" ->> "asdf" :: "long" ->> 5l :: HNil ) )
        symmetric[ABundle]( ABundle( 'int ->> 3 :: 'string ->> "asdf" :: 'long ->> 5l :: HNil ) )
    }

    it should "support Bundler" in {
        symmetric[Cat]( Cat( true ) )
    }

    it should "support Byte" in {
        symmetric[Byte]( 3.toByte )
    }

    it should "support Char" in {
        symmetric[Char]( 'c' )
    }

    it should "support CharSequence" in {
        symmetric[CharSequence]( "asdf" )
    }

    it should "support Double" in {
        symmetric[Double]( 3.14d )
    }

    it should "support Float" in {
        symmetric[Float]( 3.14f )
    }

    it should "support Int" in {
        symmetric[Int]( 3 )
    }

    it should "support Long" in {
        symmetric[Long]( 3l )
    }

    it should "support Option" in {
        symmetric[Option[Int]]( Option( 3 ) )
        symmetric[Option[Int]]( None )
    }

    it should "support Option with null values" in {
        asymmetric[String, Option[String]]( null, None )
    }

    it should "support Parcelable" in {
        symmetric[Parcelable]( Uri.parse( "http://taig.io/" ) )
        symmetric[Uri]( Uri.parse( "http://taig.io/" ) )
    }

    it should "support Short" in {
        symmetric[Short]( 3.toShort )
    }

    it should "support String" in {
        symmetric[String]( "asdf" )
    }

    it should "support Traversable" in {
        case class A( b: Byte, c: Char, d: Option[Double] )

        symmetric[Traversable[Float]]( Seq( 3.4f, 1.2f, 6.7f ) )
        symmetric[Seq[Float]]( Seq( 3.4f, 1.2f, 6.7f ) )
        symmetric[Vector[Uri]]( Vector( Uri.parse( "http://taig.io/" ) ) )
        symmetric[List[Animal]]( List( Dog( "Hoschi" ) ) )
    }

    it should "support URL" in {
        symmetric[URL]( new URL( "http://taig.io/" ) )
    }

    it should "fail when accessing a non-existing value" in {
        intercept[exception.KeyNotFound] {
            new AIntent().read[Int]( "a" )
        }
    }
}