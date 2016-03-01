package io.taig.android.soap

import android.os.Build.VERSION_CODES._
import org.robolectric.annotation.Config
import shapeless._
import shapeless.syntax.singleton._

@Config( sdk = Array( LOLLIPOP ) )
class EncoderTest extends Suite {
    it should "support ADTs" in {
        Encoder[Animal].encode( Dog( "Holly" ) ) shouldBe
            Bundle( "type" ->> classOf[Dog].getCanonicalName :: "name" ->> "Holly" :: HNil )
    }

    it should "support case classes" in {
        Encoder[Dog].encode( Dog( "Holly" ) ) shouldBe Bundle( "name", "Holly" )
        Encoder[Bird.Eagle].encode( Bird.Eagle( Some( 13.7f ), List( Mouse( 3 ) ) ) ) shouldBe
            Bundle(
                "weight" ->> 13.7f ::
                    "hunts" ->> Bundle(
                        "0",
                        Bundle( "type" ->> classOf[Mouse].getCanonicalName :: "age" ->> 3 :: HNil )
                    ) ::
                        HNil
            )
    }

    it should "support CNil (at compile time)" in {
        intercept[Throwable] {
            Encoder[CNil].encode( null )
        }
    }

    it should "support HNil" in {
        Encoder[HNil].encode( HNil ) shouldBe Bundle.empty
    }

    it should "support Arrays" in {
        Encoder[Array[Animal]].encode( Array( Dog( "Holly" ), Cat( moody = true ) ) ) shouldBe
            Bundle(
                "0" ->> Bundle( "type" ->> classOf[Dog].getCanonicalName :: "name" ->> "Holly" :: HNil ) ::
                    "1" ->> Bundle( "type" ->> classOf[Cat].getCanonicalName :: "moody" ->> true :: HNil ) ::
                    HNil
            )
    }

    it should "support Iterables" in {
        Encoder[Seq[Animal]].encode( Seq( Dog( "Holly" ), Cat( moody = true ) ) ) shouldBe
            Bundle(
                "0" ->> Bundle( "type" ->> classOf[Dog].getCanonicalName :: "name" ->> "Holly" :: HNil ) ::
                    "1" ->> Bundle( "type" ->> classOf[Cat].getCanonicalName :: "moody" ->> true :: HNil ) ::
                    HNil
            )
    }

    it should "support Maps" in {
        Encoder[Map[Int, Animal]].encode( Map( 1 → Dog( "Holly" ) ) ) shouldBe
            Bundle(
                "values" ->> Bundle( "0", Bundle( "type" ->> classOf[Dog].getCanonicalName :: "name" ->> "Holly" :: HNil ) ) ::
                    "keys" ->> Bundle( "0", 1 ) ::
                    HNil
            )
    }

    it should "support Maps with String keys" in {
        Encoder[Map[String, Int]].encode( Map( "foo" → 3, "bar" → 10, "" → 0 ) ) shouldBe
            Bundle( "foo" ->> 3 :: "bar" ->> 10 :: "" ->> 0 :: HNil )
    }
}