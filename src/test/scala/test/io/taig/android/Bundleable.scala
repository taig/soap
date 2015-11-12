package test.io.taig.android

import android.os.Build.VERSION_CODES.LOLLIPOP
import io.taig.android.parcelable._
import org.robolectric.annotation.Config
import org.scalatest._
import shapeless._

import scala.language.reflectiveCalls

@Config( sdk = Array( LOLLIPOP ) )
class Bundleable
        extends FlatSpec
        with Matchers
        with RobolectricSuite {
    def write[T: Bundleable.Write]( value: T ) = implicitly[Bundleable.Write[T]].write( value )

    def read[T: Bundleable.Read]( bundle: Bundle ) = implicitly[Bundleable.Read[T]].read( bundle )

    def bundleable[T: Bundleable.Write: Bundleable.Read]( value: T ) = read[T]( write( value ) ) shouldEqual value

    it should "support Array" in {
        bundleable( Array( Some( 3 ), None, Some( 4 ) ) )
    }

    it should "support Bundleize" in {
        bundleable( 3 )
    }

    it should "support case class" in {
        case class Person( name: String, age: Option[Int] )
        case class House( rooms: Int, inhabitants: Seq[Person] )

        bundleable( Person( "Taig", None ) )
        bundleable( House( 8, Seq( Person( "Taig", None ) ) ) )
    }

    it should "support Either" in {
        bundleable[Either[String, Int]]( Left( "fdsa" ) )
        bundleable[Either[String, Int]]( Right( 3 ) )
    }

    it should "support HNil" in {
        bundleable[HNil]( HNil )
    }

    it should "support Option" in {
        bundleable( Option( 3 ) )
        // bundleable( Some( "asdf" ) )
        // bundleable( None )
    }

    it should "support Traversable" in {
        bundleable( Seq( Some( 3 ), None, Some( 4 ) ) )
    }

    it should "support Tuple" in {
        bundleable( ( 3, "asdf" ) )
    }
}