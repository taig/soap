package io.taig.android.soap

import java.io.File
import java.net.URL

import android.net.Uri

abstract class WriterReaderTest[C](
        writerSerializable: Writer[C, File],
        readerSerializable: Reader[C, File]
)(
        implicit
        writerAnimal:       Writer[C, Animal],
        writerArrayBoolean: Writer[C, Array[Boolean]],
        writerArrayByte:    Writer[C, Array[Byte]],
        writerArrayChar:    Writer[C, Array[Char]],
        writerArrayDouble:  Writer[C, Array[Double]],
        writerArrayFloat:   Writer[C, Array[Float]],
        writerArrayInt:     Writer[C, Array[Int]],
        writerArrayLong:    Writer[C, Array[Long]],
        writerArrayShort:   Writer[C, Array[Short]],
        writerArrayString:  Writer[C, Array[String]],
        writerBoolean:      Writer[C, Boolean],
        writerBundle:       Writer[C, Bundle],
        writerByte:         Writer[C, Byte],
        writerChar:         Writer[C, Char],
        writerCharSequence: Writer[C, CharSequence],
        writerDouble:       Writer[C, Double],
        writerFloat:        Writer[C, Float],
        writerInt:          Writer[C, Int],
        writerLong:         Writer[C, Long],
        writerOptionString: Writer[C, Option[String]],
        writerArrayUri:     Writer[C, Array[Uri]],
        writerSeqInt:       Writer[C, Seq[Int]],
        writerSeqUri:       Writer[C, Seq[Uri]],
        writerSetString:    Writer[C, Set[String]],
        writerShort:        Writer[C, Short],
        writerString:       Writer[C, String],
        writerUri:          Writer[C, Uri],
        writerURL:          Writer[C, URL],
        readerAnimal:       Reader[C, Animal],
        readerArrayBoolean: Reader[C, Array[Boolean]],
        readerArrayByte:    Reader[C, Array[Byte]],
        readerArrayChar:    Reader[C, Array[Char]],
        readerArrayDouble:  Reader[C, Array[Double]],
        readerArrayFloat:   Reader[C, Array[Float]],
        readerArrayInt:     Reader[C, Array[Int]],
        readerArrayLong:    Reader[C, Array[Long]],
        readerArrayShort:   Reader[C, Array[Short]],
        readerArrayString:  Reader[C, Array[String]],
        readerBoolean:      Reader[C, Boolean],
        readerBundle:       Reader[C, Bundle],
        readerByte:         Reader[C, Byte],
        readerChar:         Reader[C, Char],
        readerCharSequence: Reader[C, CharSequence],
        readerInt:          Reader[C, Int],
        readerDouble:       Reader[C, Double],
        readerFloat:        Reader[C, Float],
        readerLong:         Reader[C, Long],
        readerArrayUri:     Reader[C, Array[Uri]],
        readerSeqInt:       Reader[C, Seq[Int]],
        readerSeqUri:       Reader[C, Seq[Uri]],
        readerSetString:    Reader[C, Set[String]],
        readerShort:        Reader[C, Short],
        readerString:       Reader[C, String],
        readerUri:          Reader[C, Uri],
        readerURL:          Reader[C, URL]
) extends Suite {
    def instance: C

    def verify[T]( values: T* )( implicit w: Writer[C, T], r: Reader[C, T] ) = {
        values.foreach { value ⇒
            val container = instance
            w.write( container, "key", value )
            r.read( container, "key" ) match {
                case Some( output ) ⇒ output shouldBe value
                case None           ⇒ fail()
            }
        }

        r.read( instance, "key" ) shouldEqual None
    }

    it should "support Arrays when Iterable is supported" in {
        verify[Array[Uri]]( Array( Uri.parse( "http://taig.io/" ) ) )
    }

    it should "support Iterable when Array is supported" in {
        verify[Seq[Int]]( Seq( 1, 2, 3 ) )
        verify[Set[String]]( Set( "foo", "bar", "" ) )
    }

    it should "support Encoder/Decoder" in {
        verify[Animal]( Dog( "Holly" ), Cat( true ) )
    }

    it should "support Boolean" in {
        verify[Boolean]( true, false )
    }

    it should "support Boolean Arrays" in {
        verify( Array( true, false, false, true ) )
    }

    it should "support Bundle" in {
        verify[Bundle]( Bundle( "key", "value" ) )
    }

    it should "support Byte" in {
        verify[Byte]( Byte.MinValue, Byte.MaxValue )
    }

    it should "support Byte Arrays" in {
        verify[Array[Byte]]( Array( Byte.MaxValue, Byte.MinValue ) )
    }

    it should "support Char" in {
        verify[Char]( Char.MinValue, Char.MaxValue )
    }

    it should "support Char Arrays" in {
        verify[Array[Char]]( Array( Char.MaxValue, Char.MinValue ) )
    }

    it should "support CharSequence" in {
        verify[CharSequence]( "", "foobar" )
    }

    it should "support Double" in {
        verify[Double]( Double.MinValue, Double.MaxValue )
    }

    it should "support Double Arrays" in {
        verify[Array[Double]]( Array( Double.MaxValue, Double.MinValue ) )
    }

    it should "support Enum" in {
        verify[Enumeration]( Enumeration.A, Enumeration.B )
    }

    it should "support Float" in {
        verify[Float]( Float.MinValue, Float.MaxValue )
    }

    it should "support Float Arrays" in {
        verify[Array[Float]]( Array( Float.MaxValue, Float.MinValue ) )
    }

    it should "support Int" in {
        verify[Int]( Int.MinValue, 0, Int.MaxValue )
    }

    it should "support Int Arrays" in {
        verify[Array[Int]]( Array( Int.MaxValue, Int.MinValue ) )
    }

    it should "support Long" in {
        verify[Long]( Long.MinValue, 0, Long.MaxValue )
    }

    it should "support Long Arrays" in {
        verify[Array[Long]]( Array( Long.MaxValue, Long.MinValue ) )
    }

    it should "support Parcelable" in {
        verify[Uri]( Uri.parse( "http://taig.io/" ) )
    }

    it should "support Parcelable Iterables" in {
        verify[Seq[Uri]]( Seq( Uri.parse( "http://taig.io/" ) ) )
    }

    it should "support Serializable" in {
        verify[File]( new File( "./foo/bar" ) )( writerSerializable, readerSerializable )
    }

    it should "support Short" in {
        verify[Short]( Short.MinValue, 0, Short.MaxValue )
    }

    it should "support Short Arrays" in {
        verify[Array[Short]]( Array( Short.MaxValue, Short.MinValue ) )
    }

    it should "support String" in {
        verify[String]( "", "foobar" )
    }

    it should "support String Arrays" in {
        verify[Array[String]]( Array( "foo", "bar", "" ) )
    }

    it should "support URL" in {
        verify[URL]( new URL( "http://taig.io/" ) )
    }
}