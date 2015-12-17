package test.io.taig.android

import android.os.Build.VERSION_CODES._
import io.taig.android.parcelable.bundler.Codec
import io.taig.android.parcelable.{ Bundle ⇒ ABundle }
import org.robolectric.annotation.Config

import scala.language.reflectiveCalls

@Config( sdk = Array( LOLLIPOP ) )
class Bundler extends Suite {
    def verify[V: Codec]( value: V ) = {
        val bundle = implicitly[Codec[V]].encode( value )
        implicitly[Codec[V]].decode( bundle ) shouldEqual value
        bundle
    }

    it should "support Array" in {
        verify[Array[Int]]( Array( 3, 4 ) )
        verify[Array[Option[Int]]]( Array( Some( 3 ), None, Some( 4 ) ) )
    }

    it should "support case class" in {
        verify[Animal]( Dog( "Hoschi" ) )
        verify[Animal]( Cat( false ) )
        verify[Bird.Eagle]( Bird.Eagle( Some( 3.4f ), List( Cat( true ), Mouse( 1 ) ) ) )
    }

    it should "support Either" in {
        verify[Either[String, Int]]( Left( "fdsa" ) )
        verify[Either[String, Int]]( Right( 3 ) )
    }

    it should "support sealed trait inheritance" in {
        verify[Animal]( Dog( "Hoschi" ) )
        verify[Animal]( Cat( false ) )
        verify[Bird]( Bird.Eagle( Some( 3.4f ), List( Cat( true ), Mouse( 1 ) ) ) )
    }

    it should "support sealed trait enums" in {
        verify[Enum]( Enum.A ) shouldEqual ABundle( "A", ABundle.empty )
    }

    it should "support Traversable" in {
        verify[List[Int]]( List( 3, 4 ) )
        verify[Seq[Option[Int]]]( Seq( Some( 3 ), None, Some( 4 ) ) )
    }

    it should "support Tuple" in {
        verify[( Int, String, Option[Float] )]( ( 3, "asdf", Some( 1f ) ) )
    }
}