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

    def bundleable[W: Bundleable.Write, R: Bundleable.Read]( value: W ) = read[R]( write[W]( value ) ) shouldEqual value

    it should "support Array" in {
        bundleable[Array[Option[Int]], Array[Option[Int]]]( Array( Some( 3 ), None, Some( 4 ) ) )
    }

    it should "support Bundleize" in {
        bundleable[String, String]( "asdf" )
        bundleable[Int, Int]( 3 )
    }

    it should "support case class" in {
        case class Person( name: String, age: Option[Int] )
        case class House( rooms: Int, inhabitants: Seq[Person] )

        bundleable[Person, Person]( Person( "Taig", None ) )
        bundleable[House, House]( House( 8, Seq( Person( "Taig", None ) ) ) )
    }

    it should "support Either" in {
        bundleable[Left[String, Int], Left[String, Int]]( Left( "fdsa" ) )
        bundleable[Left[String, Int], Either[String, Int]]( Left( "fdsa" ) )
        bundleable[Right[String, Int], Right[String, Int]]( Right( 3 ) )
        bundleable[Right[String, Int], Either[String, Int]]( Right( 3 ) )
    }

    it should "support HNil" in {
        bundleable[HNil, HNil]( HNil )
    }

    it should "support Option" in {
        bundleable[Option[Int], Option[Int]]( Option( 3 ) )
        bundleable[Option[Int], Some[Int]]( Option( 3 ) )
        bundleable[Some[String], Some[String]]( Some( "asdf" ) )
        bundleable[Some[String], Option[String]]( Some( "asdf" ) )
        bundleable[Option[String], Some[String]]( Some( "asdf" ) )
        bundleable[Option[String], Option[String]]( Some( "asdf" ) )
        bundleable[None.type, None.type]( None )
        bundleable[None.type, Option[Int]]( None )
    }

    it should "support Traversable" in {
        bundleable[Seq[Option[Int]], Seq[Option[Int]]]( Seq( Some( 3 ), None, Some( 4 ) ) )
    }

    it should "support Tuple" in {
        bundleable[( Int, String, Option[Float] ), ( Int, String, Option[Float] )]( ( 3, "asdf", Some( 1f ) ) )
    }
}