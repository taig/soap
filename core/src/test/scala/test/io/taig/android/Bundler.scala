package test.io.taig.android

import android.os.Build.VERSION_CODES._
import io.taig.android.parcelable.bundler.{ Decoder, Encoder }
import io.taig.android.parcelable.{ Bundle ⇒ ABundle }
import org.robolectric.annotation.Config
import shapeless.HNil
import shapeless.syntax.singleton._

import scala.language.reflectiveCalls

@Config( sdk = Array( LOLLIPOP ) )
class Bundler extends Suite {
    def verify[V: Encoder: Decoder]( value: V ) = {
        val bundle = implicitly[Encoder[V]].encode( value )
        implicitly[Decoder[V]].decode( bundle ) shouldEqual value
        bundle
    }

    it should "support Array" in {
        verify[Array[Int]]( Array( 3, 4 ) ) shouldEqual ABundle( "0" ->> 3 :: "1" ->> 4 :: HNil )
    }

    it should "support Array[Option]" in {
        val bundle = ABundle( "length" ->> 3 :: "0" ->> 3 :: "2" ->> 4 :: HNil )

        verify[Array[Option[Int]]]( Array( Some( 3 ), None, Some( 4 ) ) ) shouldEqual bundle
    }

    it should "support case class" in {
        verify[Dog]( Dog( "Hoschi" ) ) shouldEqual ABundle( "name", "Hoschi" )
        verify[Cat]( Cat( false ) ) shouldEqual ABundle( "moody", false )
        verify[Bird.Eagle]( Bird.Eagle( Some( 3.4f ), List( Cat( true ), Mouse( 1 ) ) ) )
    }

    it should "support Either" in {
        verify[Either[String, Int]]( Left( "fdsa" ) )
        verify[Either[String, Int]]( Right( 3 ) )
    }

    it should "support Map" in {
        val bundle = ABundle( "a" ->> 1 :: "b" ->> 2 :: "e" ->> 3 :: HNil )

        verify[Seq[( String, Int )]]( Seq( "a" → 1, "b" → 2, "e" → 3 ) ) shouldEqual bundle
        verify[Map[String, Int]]( Map( "a" → 1, "b" → 2, "e" → 3 ) ) shouldEqual bundle
    }

    it should "support sealed trait inheritance" in {
        verify[Animal]( Dog( "Hoschi" ) ) shouldEqual ABundle( classOf[Dog].getCanonicalName, ABundle( "name", "Hoschi" ) )
        verify[Animal]( Cat( false ) ) shouldEqual ABundle( classOf[Cat].getCanonicalName, ABundle( "moody", false ) )
        verify[Animal]( Bird.Eagle( Some( 3.4f ), List( Cat( true ), Mouse( 1 ) ) ) )
    }

    it should "support sealed trait hierarchies with duplicate names" in {
        verify[Vehicle]( Car( 4 ) )
        verify[Vehicle]( Military.Car( 60000 ) )
    }

    it should "support sealed trait enums" in {
        verify[Enum]( Enum.A ) shouldEqual ABundle( Enum.A.getClass.getCanonicalName, ABundle.empty )
    }

    it should "support Traversable" in {
        verify[List[Int]]( List( 3, 4 ) ) shouldEqual ABundle( "0" ->> 3 :: "1" ->> 4 :: HNil )
    }

    it should "support Traversable[Option]" in {
        val bundle = ABundle( "length" ->> 3 :: "0" ->> 3 :: "2" ->> 4 :: HNil )

        verify[Seq[Option[Int]]]( Seq( Some( 3 ), None, Some( 4 ) ) ) shouldEqual bundle
    }

    it should "support Tuple" in {
        verify[( Int, String, Option[Float] )]( ( 3, "asdf", Some( 1f ) ) )
    }
}