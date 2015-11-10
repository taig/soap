package test.io.taig.android

import android.os.Build.VERSION_CODES.LOLLIPOP
import io.taig.android.parcelable.Bundleable.from

import org.robolectric.annotation.Config
import org.scalatest._

import scala.language.reflectiveCalls

@Config( sdk = Array( LOLLIPOP ) )
class Bundleable
        extends FlatSpec
        with Matchers
        with RobolectricSuite {
    //    it should "allow to bundle case classes" in {
    //        case class Data( a: String, b: Int, c: Long )
    //
    //        implicit val b = from[Data]
    //        val instance = Data( "asdf", 3, 5l )
    //        val bundle = b.write( instance )
    //
    //        b.read( bundle ) shouldEqual instance
    //    }
    //
    //    it should "allow to bundle Tuples" in {
    //        implicit val b = from[( String, Int, Long )]
    //        val instance = ( "asdf", 3, 5l )
    //        val bundle = b.write( instance )
    //
    //        b.read( bundle ) shouldEqual instance
    //    }

    //    it should "allow to bundle Options" in {
    //        implicit val b = from[Option[( String, Int, Long )]]( io.taig.android.parcelable.Bundleable.`Bundleable[Bundleize]` )
    //        val instance1 = Some( "asdf", 3, 5l )
    //        val instance2 = None
    //
    //        b.read( b.write( instance1 ) ) shouldEqual instance1
    //        b.read( b.write( instance2 ) ) shouldEqual instance2
    //    }

    //    it should "allow to bundle Bundleizes" in {
    //        implicit val b = from[Int]
    //        val instance = 3
    //        val bundle = b.write( instance )
    //
    //        b.read( bundle ) shouldEqual instance
    //    }
}
