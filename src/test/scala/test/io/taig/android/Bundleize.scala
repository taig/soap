package test.io.taig.android

import android.net.Uri
import android.os.Build.VERSION_CODES.LOLLIPOP
import android.util.{ Size, SizeF }
import io.taig.android.parcelable._
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
    def write[T: Bundleize.Write]( value: T ) = Bundle( "value", value )

    def read[T: Bundleize.Read]( bundle: Bundle ) = bundle.read[T]( "value" )

    def bundleize[W: Bundleize.Write, R: Bundleize.Read]( value: W ) = read[R]( write[W]( value ) ) shouldEqual value

    it should "support Array" in {
        bundleize[Array[Int], Array[Int]]( Array( 1, 2, 3 ) )
    }

    it should "support Boolean" in {
        bundleize[Boolean, Boolean]( true )
        bundleize[Boolean, Boolean]( false )
    }

    it should "support Bundle" in {
        bundleize[Bundle, Bundle]( Bundle( "int" ->> 3 :: "string" ->> "asdf" :: "long" ->> 5l :: HNil ) )
    }

    it should "support Bundleable" in {
        bundleize[Option[Int], Option[Int]]( Option( 3 ) )
    }

    it should "support Byte" in {
        bundleize[Byte, Byte]( 3.toByte )
    }

    it should "support Char" in {
        bundleize[Char, Char]( 'c' )
    }

    it should "support CharSequence" in {
        bundleize[CharSequence, CharSequence]( "asdf" )
        bundleize[String, CharSequence]( "asdf" )
    }

    it should "support Double" in {
        bundleize[Double, Double]( 3.14d )
    }

    it should "support Float" in {
        bundleize[Float, Float]( 3.14f )
    }

    it should "support Int" in {
        bundleize[Int, Int]( 3 )
    }

    it should "support Long" in {
        bundleize[Long, Long]( 3l )
    }

    it should "support Parcelable" in {
        bundleize[android.os.Parcelable, android.os.Parcelable]( Uri.parse( "http://taig.io/" ) )
        bundleize[Uri, android.os.Parcelable]( Uri.parse( "http://taig.io/" ) )
        bundleize[Uri, Uri]( Uri.parse( "http://taig.io/" ) )
    }

    it should "support Short" in {
        bundleize[Short, Short]( 3.toShort )
    }

    it should "support Size" in {
        bundleize[Size, Size]( new Size( 16, 9 ) )
    }

    it should "support SizeF" in {
        bundleize[SizeF, SizeF]( new SizeF( 16.1f, 9.2f ) )
    }

    it should "support String" in {
        bundleize[String, String]( "asdf" )
    }

    it should "support Traversable" in {
        bundleize[Traversable[Float], Traversable[Float]]( Seq( 3.4f, 1.2f, 6.7f ) )
        bundleize[Seq[Float], Traversable[Float]]( Seq( 3.4f, 1.2f, 6.7f ) )
    }

    it should "fail when accessing an non-existant value" in {
        intercept[IllegalStateException] {
            Bundle.empty.read[Int]( "a" )
        }
    }
}