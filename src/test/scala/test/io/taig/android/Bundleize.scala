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
    it should "support Arrays" in {
        val instance = Array( 1, 2, 3 )
        val b = Bundle( "value" ->> instance :: HNil )
        b.read[Array[Int]]( "value" ) shouldEqual instance
    }

    it should "support Boolean" in {
        val b = Bundle( "true" ->> true :: "false" ->> false :: HNil )
        b.read[Boolean]( "true" ) shouldEqual true
        b.read[Boolean]( "false" ) shouldEqual false
    }

    it should "support Bundle" in {
        val instance = Bundle( "int" ->> 3 :: "string" ->> "asdf" :: "long" ->> 5l :: HNil )
        val b = Bundle( "value" ->> instance :: HNil )
        b.read[Bundle]( "value" ) shouldEqual instance
    }

    it should "support Byte" in {
        val instance = 3.toByte
        val b = Bundle( "value" ->> instance :: HNil )
        b.read[Byte]( "value" ) shouldEqual instance
    }

    it should "support Char" in {
        val instance = 'c'
        val b = Bundle( "value" ->> instance :: HNil )
        b.read[Char]( "value" ) shouldEqual instance
    }

    it should "support CharSequence" in {
        val instance: CharSequence = "asdf"
        val b = Bundle( "value" ->> instance :: HNil )
        b.read[CharSequence]( "value" ) shouldEqual instance
    }

    it should "support Double" in {
        val instance = 3.14d
        val b = Bundle( "value" ->> instance :: HNil )
        b.read[Double]( "value" ) shouldEqual instance
    }

    it should "support Either" in {
        val instance1: Either[String, Int] = Left( "fdsa" )
        val b1 = Bundle( "value" ->> instance1 :: HNil )
        val instance2 = Left( "asdf" )
        val b2 = Bundle( "value" ->> instance2 :: HNil )
        val instance3 = Right( Some( 3 ) )
        val b3 = Bundle( "value" ->> instance3 :: HNil )

        b1.read[Either[String, Int]]( "value" ) shouldEqual instance1
        b2.read[Either[String, Int]]( "value" ) shouldEqual instance2
        b3.read[Either[String, Option[Int]]]( "value" ) shouldEqual instance3
    }

    it should "support Float" in {
        val instance = 3.14f
        val b = Bundle( "value" ->> instance :: HNil )
        b.read[Float]( "value" ) shouldEqual instance
    }

    it should "support Int" in {
        val instance = 3
        val b = Bundle( "value" ->> instance :: HNil )
        b.read[Int]( "value" ) shouldEqual instance
    }

    it should "support Long" in {
        val instance = 3l
        val b = Bundle( "value" ->> instance :: HNil )
        b.read[Long]( "value" ) shouldEqual instance
    }

    it should "support Option" in {
        val instance1 = Some( "asdf" )
        val b1 = Bundle( "value" ->> instance1 :: HNil )
        val instance2 = None
        val b2 = Bundle( "value" ->> instance2 :: HNil )

        b1.read[Option[String]]( "value" ) shouldEqual instance1
        b2.read[Option[Int]]( "value" ) shouldEqual instance2
    }

    it should "support Parcelable" in {
        val instance = Uri.parse( "http://taig.io/" )
        val b = Bundle( "value" ->> instance :: HNil )
        b.read[Uri]( "value" ) shouldEqual instance
    }

    it should "support Short" in {
        val instance = 3.toShort
        val b = Bundle( "value" ->> instance :: HNil )
        b.read[Short]( "value" ) shouldEqual instance
    }

    it should "support Size" in {
        val instance = new Size( 16, 9 )
        val b = Bundle( "value" ->> instance :: HNil )
        b.read[Size]( "value" ) shouldEqual instance
    }

    it should "support SizeF" in {
        val instance = new SizeF( 16.1f, 9.2f )
        val b = Bundle( "value" ->> instance :: HNil )
        b.read[SizeF]( "value" ) shouldEqual instance
    }

    it should "support String" in {
        val instance = "asdf"
        val b = Bundle( "value" ->> instance :: HNil )
        b.read[String]( "value" ) shouldEqual instance
    }

    it should "support Traversable" in {
        val instance = Seq( 3.4f, 1.2f, 6.7f )
        val b = Bundle( "value" ->> instance :: HNil )
        b.read[Seq[Float]]( "value" ) shouldEqual instance
    }

    it should "fail when accessing an non-existant value" in {
        intercept[IllegalStateException] {
            new Bundle().read[Int]( "a" )
        }
    }
}