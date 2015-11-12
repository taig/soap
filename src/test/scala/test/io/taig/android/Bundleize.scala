package test.io.taig.android

import android.net.Uri
import android.os.Build.VERSION_CODES.LOLLIPOP
import android.util.{ Size, SizeF }
import io.taig.android.parcelable._
import io.taig.android.parcelable.bundleize.{ Read, Write }
import org.robolectric.annotation.Config
import org.scalatest._
import shapeless._
import shapeless.syntax.singleton._

import scala.language.reflectiveCalls

@Config( sdk = Array( LOLLIPOP ) )
class Bundleize
        extends FlatSpec
        with Matchers
        with RobolectricSuite {
    def write[T: Write]( value: T ) = Bundle( "value", value )

    def read[T: Read]( bundle: Bundle ) = bundle.read[T]( "value" )

    def test[W: Write, R: Read]( value: W ) = read[R]( write[W]( value ) ) shouldEqual value

    it should "support Array" in {
        test[Array[Int], Array[Int]]( Array( 1, 2, 3 ) )
    }

    it should "support Boolean" in {
        test[Boolean, Boolean]( true )
        test[Boolean, Boolean]( false )
    }

    it should "support Bundle" in {
        test[Bundle, Bundle]( Bundle( "int" ->> 3 :: "string" ->> "asdf" :: "long" ->> 5l :: HNil ) )
    }

    it should "support Bundleable" in {
        test[Option[Int], Option[Int]]( Option( 3 ) )
    }

    it should "support Byte" in {
        test[Byte, Byte]( 3.toByte )
    }

    it should "support Char" in {
        test[Char, Char]( 'c' )
    }

    it should "support CharSequence" in {
        test[CharSequence, CharSequence]( "asdf" )
        test[String, CharSequence]( "asdf" )
    }

    it should "support Double" in {
        test[Double, Double]( 3.14d )
    }

    it should "support Float" in {
        test[Float, Float]( 3.14f )
    }

    it should "support Int" in {
        test[Int, Int]( 3 )
    }

    it should "support Long" in {
        test[Long, Long]( 3l )
    }

    it should "support Parcelable" in {
        test[android.os.Parcelable, android.os.Parcelable]( Uri.parse( "http://taig.io/" ) )
        test[Uri, android.os.Parcelable]( Uri.parse( "http://taig.io/" ) )
        test[Uri, Uri]( Uri.parse( "http://taig.io/" ) )
    }

    it should "support Short" in {
        test[Short, Short]( 3.toShort )
    }

    it should "support Size" in {
        test[Size, Size]( new Size( 16, 9 ) )
    }

    it should "support SizeF" in {
        test[SizeF, SizeF]( new SizeF( 16.1f, 9.2f ) )
    }

    it should "support String" in {
        test[String, String]( "asdf" )
    }

    it should "support Traversable" in {
        test[Traversable[Float], Traversable[Float]]( Seq( 3.4f, 1.2f, 6.7f ) )
        test[Seq[Float], Traversable[Float]]( Seq( 3.4f, 1.2f, 6.7f ) )
    }

    it should "fail when accessing an non-existant value" in {
        intercept[IllegalStateException] {
            Bundle.empty.read[Int]( "a" )
        }
    }
}