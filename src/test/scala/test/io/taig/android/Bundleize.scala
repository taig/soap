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

    def bundleize[T: Bundleize.Write: Bundleize.Read]( value: T ) = read( write( value ) ) shouldEqual value

    it should "support Array" in {
        bundleize( Array( 1, 2, 3 ) )
    }

    it should "support Boolean" in {
        bundleize( true )
        bundleize( false )
    }

    it should "support Bundle" in {
        bundleize( Bundle( "int" ->> 3 :: "string" ->> "asdf" :: "long" ->> 5l :: HNil ) )
    }

    it should "support Bundleable" in {
        bundleize( Option( 3 ) )
        // bundleize( Some( "asdf" ) )
        // bundleize( None )
    }

    it should "support Byte" in {
        bundleize( 3.toByte )
    }

    it should "support Char" in {
        bundleize( 'c )
    }

    it should "support CharSequence" in {
        bundleize[CharSequence]( "asdf" )
    }

    it should "support Double" in {
        bundleize( 3.14d )
    }

    it should "support Float" in {
        bundleize( 3.14f )
    }

    it should "support Int" in {
        bundleize( 3 )
    }

    it should "support Long" in {
        bundleize( 3l )
    }

    it should "support Parcelable" in {
        bundleize( Uri.parse( "http://taig.io/" ) )
    }

    it should "support Short" in {
        bundleize( 3.toShort )
    }

    it should "support Size" in {
        bundleize( new Size( 16, 9 ) )
    }

    it should "support SizeF" in {
        bundleize( new SizeF( 16.1f, 9.2f ) )
    }

    it should "support String" in {
        bundleize( "asdf" )
    }

    it should "support Traversable" in {
        bundleize( Seq( 3.4f, 1.2f, 6.7f ) )
    }

    it should "fail when accessing an non-existant value" in {
        intercept[IllegalStateException] {
            Bundle.empty.read[Int]( "a" )
        }
    }
}