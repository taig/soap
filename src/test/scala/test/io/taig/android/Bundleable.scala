package test.io.taig.android

import android.os.Build.VERSION_CODES.LOLLIPOP
import io.taig.android.parcelable.Bundleable.from
import org.robolectric.annotation.Config
import org.scalatest._
import shapeless._

import scala.language.reflectiveCalls

@Config( sdk = Array( LOLLIPOP ) )
class Bundleable
        extends FlatSpec
        with Matchers
        with RobolectricSuite {
    it should "support case classes" in {
        case class Data( a: Int, b: String, c: Float )
        val bundleable = from[Data]
        val instance = Data( 3, "asdf", 3.14f )
        val b = bundleable.write( instance )
        bundleable.read( b ) shouldEqual instance
    }

    it should "support Either" in {
        val bundleable = from[Either[String, Int]]
        val instance1: Either[String, Int] = Left( "fdsa" )
        val b1 = bundleable.write( instance1 )
        val instance2: Either[String, Int] = Right( 3 )
        val b2 = bundleable.write( instance2 )

        bundleable.read( b1 ) shouldEqual instance1
        bundleable.read( b2 ) shouldEqual instance2
    }

    it should "support HNil" in {
        val bundleable = from[HNil]
        val b = bundleable.write( HNil )
        bundleable.read( b ) shouldEqual HNil
    }

    it should "support Option" in {
        val bundleable = from[Option[String]]
        val instance1 = Some( "asdf" )
        val b1 = bundleable.write( instance1 )
        val instance2 = None
        val b2 = bundleable.write( instance2 )

        bundleable.read( b1 ) shouldEqual instance1
        bundleable.read( b2 ) shouldEqual instance2
    }

    it should "support Tuple" in {
        val bundleable = from[( Int, String )]
        val instance = ( 3, "asdf" )
        val b = bundleable.write( instance )
        bundleable.read( b ) shouldEqual instance
    }
}