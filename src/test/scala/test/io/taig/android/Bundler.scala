package test.io.taig.android

import android.os.Build.VERSION_CODES._
import io.taig.android.parcelable.bundler.{ Decoder, Encoder }
import org.robolectric.annotation.Config

import scala.language.reflectiveCalls

@Config( sdk = Array( LOLLIPOP ) )
class Bundler extends Suite {
    def symmetric[T: Encoder: Decoder]( value: T ) = asymmetric[T, T]( value, value )

    def asymmetric[I: Encoder, O: Decoder]( value: I, result: O ) = {
        val bundle = implicitly[Encoder[I]].encode( value )
        implicitly[Decoder[O]].decode( bundle ) shouldEqual result
        bundle
    }

    it should "support Array" in {
        symmetric[Array[Option[Int]]]( Array( Some( 3 ), None, Some( 4 ) ) )
    }

    it should "support case class" in {
        symmetric[Animal]( Dog( "Hoschi" ) )
        symmetric[Animal]( Cat( false ) )
        symmetric[Bird.Eagle]( Bird.Eagle( Some( 3.4f ), List( Cat( true ), Mouse( 1 ) ) ) )
    }

    it should "support Either" in {
        symmetric[Either[String, Int]]( Left( "fdsa" ) )
        symmetric[Either[String, Int]]( Right( 3 ) )
    }

    it should "support trait inheritance" in {
        symmetric[Animal]( Dog( "Hoschi" ) )
        symmetric[Animal]( Cat( false ) )
        symmetric[Bird]( Bird.Eagle( Some( 3.4f ), List( Cat( true ), Mouse( 1 ) ) ) )
    }

    it should "support Traversable" in {
        symmetric[Seq[Option[Int]]]( Seq( Some( 3 ), None, Some( 4 ) ) )
    }

    it should "support Tuple" in {
        symmetric[( Int, String, Option[Float] )]( ( 3, "asdf", Some( 1f ) ) )
    }
}