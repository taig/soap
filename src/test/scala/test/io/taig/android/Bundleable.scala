package test.io.taig.android

import android.os.Build.VERSION_CODES.LOLLIPOP
import io.taig.android.parcelable._
import io.taig.android.parcelable.bundleable.{ Read, Write }
import org.robolectric.annotation.Config
import org.scalatest._
import shapeless._

import scala.language.reflectiveCalls

@Config( sdk = Array( LOLLIPOP ) )
class Bundleable
        extends FlatSpec
        with Matchers
        with RobolectricSuite {
    def write[T: Write]( value: T ) = implicitly[Write[T]].write( value )

    def read[T: Read]( bundle: Bundle ) = implicitly[Read[T]].read( bundle )

    def test[W: Write, R: Read]( value: W ) = read[R]( write[W]( value ) ) shouldEqual value

    it should "support Array" in {
        test[Array[Option[Int]], Array[Option[Int]]]( Array( Some( 3 ), None, Some( 4 ) ) )
    }

    it should "support Bundleize" in {
        test[String, String]( "asdf" )
        test[Int, Int]( 3 )
    }

    it should "support case class" in {
        case class Person( name: String, age: Option[Int] )
        case class House( rooms: Int, inhabitants: Seq[Person] )

        test[Person, Person]( Person( "Taig", None ) )
        test[House, House]( House( 8, Seq( Person( "Taig", None ) ) ) )
    }

    it should "support Either" in {
        test[Left[String, Int], Left[String, Int]]( Left( "fdsa" ) )
        test[Left[String, Int], Either[String, Int]]( Left( "fdsa" ) )
        test[Right[String, Int], Right[String, Int]]( Right( 3 ) )
        test[Right[String, Int], Either[String, Int]]( Right( 3 ) )
    }

    it should "support HNil" in {
        test[HNil, HNil]( HNil )
    }

    it should "support trait inheritance" in {
        sealed trait Animal
        case class Dog( name: String ) extends Animal
        case class Cat( friendly: Boolean ) extends Animal

        test[Animal, Animal]( Dog( "Hoschi" ) )
        test[Animal, Animal]( Cat( false ) )
    }

    it should "support Option" in {
        test[Option[Int], Option[Int]]( Option( 3 ) )
        test[Option[Int], Some[Int]]( Option( 3 ) )
        test[Some[String], Some[String]]( Some( "asdf" ) )
        test[Some[String], Option[String]]( Some( "asdf" ) )
        test[Option[String], Some[String]]( Some( "asdf" ) )
        test[Option[String], Option[String]]( Some( "asdf" ) )
        test[None.type, None.type]( None )
        test[None.type, Option[Int]]( None )
    }

    it should "support Traversable" in {
        test[Seq[Option[Int]], Seq[Option[Int]]]( Seq( Some( 3 ), None, Some( 4 ) ) )
    }

    it should "support Tuple" in {
        test[( Int, String, Option[Float] ), ( Int, String, Option[Float] )]( ( 3, "asdf", Some( 1f ) ) )
    }
}