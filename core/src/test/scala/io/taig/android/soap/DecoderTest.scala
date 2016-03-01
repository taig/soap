package io.taig.android.soap

import android.os.Build.VERSION_CODES._
import org.robolectric.annotation.Config
import shapeless.{ CNil, HNil }
import shapeless.syntax.singleton._

@Config( sdk = Array( LOLLIPOP ) )
class DecoderTest extends Suite {
    it should "support ADTs" in {
        Decoder[Animal].decode( Bundle( "type" ->> classOf[Dog].getCanonicalName :: "name" ->> "Holly" :: HNil ) ) shouldBe
            Some( Dog( "Holly" ) )

        Decoder[Animal].decode( Bundle( "name" ->> "Holly" :: HNil ) ) shouldBe None
    }

    it should "support case classes" in {
        Decoder[Dog].decode( Bundle( "name", "Holly" ) ) shouldBe Some( Dog( "Holly" ) )
        Decoder[Dog].decode( Bundle( "noname", "Holly" ) ) shouldBe None

        Decoder[Bird.Eagle].decode(
            Bundle(
                "weight" ->> 13.7f ::
                    "hunts" ->> Bundle(
                        "0",
                        Bundle( "type" ->> classOf[Mouse].getCanonicalName :: "age" ->> 3 :: HNil )
                    ) ::
                        HNil
            )
        ) shouldBe Some( Bird.Eagle( Some( 13.7f ), List( Mouse( 3 ) ) ) )
    }

    it should "support CNil (at compile time)" in {
        intercept[Throwable] {
            Decoder[CNil].decode( Bundle.empty )
        }
    }

    it should "support HNil" in {
        Decoder[HNil].decode( Bundle.empty ) shouldBe Some( HNil )
    }

    it should "support Arrays" in {
        Decoder[Array[Animal]].decode(
            Bundle(
                "0" ->> Bundle( "type" ->> classOf[Dog].getCanonicalName :: "name" ->> "Holly" :: HNil ) ::
                    "1" ->> Bundle( "type" ->> classOf[Cat].getCanonicalName :: "moody" ->> true :: HNil ) ::
                    HNil
            )
        ) match {
                case Some( output ) ⇒ output shouldBe Array( Dog( "Holly" ), Cat( moody = true ) )
                case None           ⇒ fail()
            }
    }

    it should "support Iterables" in {
        Decoder[Seq[Animal]].decode(
            Bundle(
                "0" ->> Bundle( "type" ->> classOf[Dog].getCanonicalName :: "name" ->> "Holly" :: HNil ) ::
                    "1" ->> Bundle( "type" ->> classOf[Cat].getCanonicalName :: "moody" ->> true :: HNil ) ::
                    HNil
            )
        ) shouldBe Some( Seq( Dog( "Holly" ), Cat( moody = true ) ) )
    }

    it should "support Maps" in {
        Decoder[Map[Int, Animal]].decode(
            Bundle(
                "values" ->> Bundle( "0", Bundle( "type" ->> classOf[Dog].getCanonicalName :: "name" ->> "Holly" :: HNil ) ) ::
                    "keys" ->> Bundle( "0", 1 ) ::
                    HNil
            )
        ) shouldBe Some( Map( 1 → Dog( "Holly" ) ) )
    }

    it should "support Maps with String keys" in {
        Decoder[Map[String, Int]].decode( Bundle( "foo" ->> 3 :: "bar" ->> 10 :: "" ->> 0 :: HNil ) ) shouldBe
            Some( Map( "foo" → 3, "bar" → 10, "" → 0 ) )
    }
}