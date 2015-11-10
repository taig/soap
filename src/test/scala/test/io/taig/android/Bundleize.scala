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
        val i = Array( 1, 2, 3 )
        val b = Bundle( "value" ->> i :: HNil )
        b.read[Array[Int]]( "value" ) shouldEqual i
    }

    it should "support Boolean" in {
        val b = Bundle( "true" ->> true :: "false" ->> false :: HNil )
        b.read[Boolean]( "true" ) shouldEqual true
        b.read[Boolean]( "false" ) shouldEqual false
    }

    it should "support Bundle" in {
        val i = Bundle( "int" ->> 3 :: "string" ->> "asdf" :: "long" ->> 5l :: HNil )
        val b = Bundle( "value" ->> i :: HNil )
        b.read[Bundle]( "value" ) shouldEqual i
    }

    it should "support Bundleable" in {
        val i1: Option[Int] = Some( 3 )
        val b1 = Bundle( "value" ->> i1 :: HNil )
        val i2: Either[Int, String] = Right( "asdf" )
        val b2 = Bundle( "value" ->> i2 :: HNil )
        val i3: Option[Option[Uri]] = Some( Some( Uri.parse( "http://taig.io/" ) ) )
        val b3 = Bundle( "value" ->> i3 :: HNil )
        b1.read[Option[Int]]( "value" ) shouldEqual i1
        b2.read[Either[Int, String]]( "value" ) shouldEqual i2
        b3.read[Option[Option[Uri]]]( "value" ) shouldEqual i3
    }

    it should "support Byte" in {
        val i = 3.toByte
        val b = Bundle( "value" ->> i :: HNil )
        b.read[Byte]( "value" ) shouldEqual i
    }

    it should "support Char" in {
        val i = 'c'
        val b = Bundle( "value" ->> i :: HNil )
        b.read[Char]( "value" ) shouldEqual i
    }

    it should "support CharSequence" in {
        val i: CharSequence = "asdf"
        val b = Bundle( "value" ->> i :: HNil )
        b.read[CharSequence]( "value" ) shouldEqual i
    }

    it should "support Double" in {
        val i = 3.14d
        val b = Bundle( "value" ->> i :: HNil )
        b.read[Double]( "value" ) shouldEqual i
    }

    it should "support Float" in {
        val i = 3.14f
        val b = Bundle( "value" ->> i :: HNil )
        b.read[Float]( "value" ) shouldEqual i
    }

    it should "support Int" in {
        val i = 3
        val b = Bundle( "value" ->> i :: HNil )
        b.read[Int]( "value" ) shouldEqual i
    }

    it should "support Long" in {
        val i = 3l
        val b = Bundle( "value" ->> i :: HNil )
        b.read[Long]( "value" ) shouldEqual i
    }

    it should "support Parcelable" in {
        val i = Uri.parse( "http://taig.io/" )
        val b = Bundle( "value" ->> i :: HNil )
        b.read[Uri]( "value" ) shouldEqual i
    }

    it should "support Short" in {
        val i = 3.toShort
        val b = Bundle( "value" ->> i :: HNil )
        b.read[Short]( "value" ) shouldEqual i
    }

    it should "support Size" in {
        val i = new Size( 16, 9 )
        val b = Bundle( "value" ->> i :: HNil )
        b.read[Size]( "value" ) shouldEqual i
    }

    it should "support SizeF" in {
        val i = new SizeF( 16.1f, 9.2f )
        val b = Bundle( "value" ->> i :: HNil )
        b.read[SizeF]( "value" ) shouldEqual i
    }

    it should "support String" in {
        val i = "asdf"
        val b = Bundle( "value" ->> i :: HNil )
        b.read[String]( "value" ) shouldEqual i
    }

    it should "support Traversable" in {
        val i = Seq( 3.4f, 1.2f, 6.7f )
        val b = Bundle( "value" ->> i :: HNil )
        b.read[Seq[Float]]( "value" ) shouldEqual i
    }

    it should "fail when accessing an non-existant value" in {
        intercept[IllegalStateException] {
            Bundle.empty.read[Int]( "a" )
        }
    }
}