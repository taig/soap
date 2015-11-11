package test.io.taig.android

import android.os.Build.VERSION_CODES.LOLLIPOP
import io.taig.android.parcelable.Bundleable.from
import io.taig.android.parcelable._
import org.robolectric.annotation.Config
import org.scalatest._
import shapeless._
import shapeless.syntax.singleton._

import scala.language.reflectiveCalls

@Config( sdk = Array( LOLLIPOP ) )
class Bundleable
        extends FlatSpec
        with Matchers
        with RobolectricSuite {
    it should "support Array" in {
        val bundleable = from[Array[Int]]
        val instance = Array( 3, 1, 4 )
        val b = bundleable.write( instance )
        bundleable.read( b ) shouldEqual instance
    }

    it should "support Bundleize" in {
        val bundleable = from[Int]
        val b = bundleable.write( 3 )
        b shouldEqual Bundle( "value" ->> 3 :: HNil )
        bundleable.read( b ) shouldEqual 3
    }

    it should "support case class" in {
        {
            case class Data( a: Int, b: String, c: Float )
            val bundleable = from[Data]
            val instance = Data( 3, "asdf", 3.14f )
            val b = bundleable.write( instance )
            b shouldEqual Bundle( "a" ->> 3 :: "b" ->> "asdf" :: "c" ->> 3.14f :: HNil )
            bundleable.read( b ) shouldEqual instance
        }

        {
            case class Person( name: String, age: Option[Int] )
            case class House( rooms: Int, inhabitants: Seq[Person] )
            val bundleable = from[House]
            val instance = House( 8, Seq( Person( "Taig", None ) ) )
            val b = bundleable.write( instance )
            bundleable.read( b ) shouldEqual instance
        }
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

        b1 shouldEqual Bundle( "option" ->> 1 :: "value" ->> "asdf" :: HNil )
        bundleable.read( b1 ) shouldEqual instance1
        bundleable.read( b2 ) shouldEqual instance2
    }

    it should "support Traversable" in {
        val bundleable = from[Seq[Int]]
        val instance = Seq( 3, 1, 4 )
        val b = bundleable.write( instance )
        bundleable.read( b ) shouldEqual instance
    }

    it should "support Tuple" in {
        val bundleable = from[( Int, String )]
        val instance = ( 3, "asdf" )
        val b = bundleable.write( instance )
        bundleable.read( b ) shouldEqual instance
    }
}