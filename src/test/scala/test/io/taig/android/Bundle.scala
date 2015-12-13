package test.io.taig.android

import java.net.URL

import android.net.Uri
import android.os.Build.VERSION_CODES._
import android.os.Parcelable
import android.util.{ SizeF, Size }
import io.taig.android.parcelable._
import io.taig.android.parcelable.{ Bundle ⇒ ABundle }
import io.taig.android.parcelable.bundle.Encoder
import io.taig.android.parcelable.bundle.Decoder
import org.robolectric.annotation.Config
import org.scalatest.{ FlatSpec, Matchers, RobolectricSuite }
import shapeless._
import shapeless.syntax.singleton._

@Config( sdk = Array( LOLLIPOP ) )
class Bundle extends FlatSpec
        with Matchers
        with RobolectricSuite {
    def symmetric[T: Encoder: Decoder]( value: T ) = asymmetric[T, T]( value, value )

    def asymmetric[I: Encoder, O: Decoder]( value: I, result: O ) = {
        val bundle = Bundle( "value", value )
        bundle.read[O]( "value" ) shouldEqual result
        bundle
    }

    it should "support Array" in {
        case class A( b: Byte, c: Char, d: Option[Double] )

        symmetric[Array[Int]]( Array( 1, 2, 3 ) )
        symmetric[Array[String]]( Array( "1", "2", "3" ) )
        symmetric[Array[Uri]]( Array( Uri.parse( "http://taig.io/" ) ) )
        symmetric[Array[A]]( Array( A( Byte.MinValue, 'a', None ) ) )
    }

    it should "support Boolean" in {
        symmetric[Boolean]( true )
        symmetric[Boolean]( false )
    }

    it should "support Bundle" in {
        symmetric[ABundle]( ABundle( "int" ->> 3 :: "string" ->> "asdf" :: "long" ->> 5l :: HNil ) )
    }

    it should "support Bundler" in {
        case class A( b: Byte, c: Char, d: Option[Double] )

        symmetric[A]( A( Byte.MaxValue, 'c', Some( 3.14d ) ) )
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

    it should "support Size" in {
        symmetric[Size]( new Size( 16, 9 ) )
    }

    it should "support SizeF" in {
        symmetric[SizeF]( new SizeF( 16.1f, 9.2f ) )
    }

    it should "support String" in {
        symmetric[String]( "asdf" )
    }

    it should "support Traversable" in {
        case class A( b: Byte, c: Char, d: Option[Double] )

        symmetric[Traversable[Float]]( Seq( 3.4f, 1.2f, 6.7f ) )
        symmetric[Seq[Float]]( Seq( 3.4f, 1.2f, 6.7f ) )
        symmetric[Vector[Uri]]( Vector( Uri.parse( "http://taig.io/" ) ) )
        symmetric[List[A]]( List( A( Byte.MinValue, 'a', None ) ) )
    }

    it should "support URL" in {
        symmetric[URL]( new URL( "http://taig.io/" ) )
    }

    it should "fail when accessing a non-existing value" in {
        intercept[exception.KeyNotFound] {
            Bundle.empty.read[Int]( "a" )
        }
    }
}