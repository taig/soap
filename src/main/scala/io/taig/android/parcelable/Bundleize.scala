package io.taig.android.parcelable

import android.annotation.TargetApi
import android.os.{ IBinder, Bundle }
import android.util.{ Log, SizeF, Size }

import scala.collection._
import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import scala.reflect.ClassTag
import scala.reflect.classTag
import scala.util.Try

trait Bundleize[T] {
    def read( key: String, bundle: Bundle ): T

    def write( key: String, value: T, bundle: Bundle ): Unit
}

object Bundleize {
    def apply[T]( r: ( Bundle, String ) ⇒ T, w: ( Bundle, String, T ) ⇒ Unit ): Bundleize[T] = new Bundleize[T] {
        override def read( key: String, bundle: Bundle ) = r( bundle, key )

        override def write( key: String, value: T, bundle: Bundle ) = w( bundle, key, value )
    }

    private def bailOut( key: String ) = throw new IllegalStateException(
        s"Key '$key' does not exist, use Option instead or get your shit together"
    )

    implicit val `Bundleize[Boolean]` = Bundleize[Boolean](
        ( bundle, key ) ⇒ bundle.getBoolean( key, bailOut( key ) ),
        _.putBoolean( _, _ )
    )

    implicit val `Bundleize[Bundle]` = Bundleize[Bundle](
        ( bundle, key ) ⇒ Option( bundle.getBundle( key ) ).getOrElse( bailOut( key ) ),
        _.putBundle( _, _ )
    )

    implicit val `Bundleize[Byte]` = Bundleize[Byte](
        ( bundle, key ) ⇒ bundle.getByte( key, bailOut( key ) ),
        _.putByte( _, _ )
    )

    implicit val `Bundleize[Char]` = Bundleize[Char](
        ( bundle, key ) ⇒ bundle.getChar( key, bailOut( key ) ),
        _.putChar( _, _ )
    )

    implicit val `Bundleize[CharSequence]` = Bundleize[CharSequence](
        ( bundle, key ) ⇒ bundle.getCharSequence( key, bailOut( key ) ),
        _.putCharSequence( _, _ )
    )

    implicit val `Bundleize[Double]` = Bundleize[Double](
        ( bundle, key ) ⇒ bundle.getDouble( key, bailOut( key ) ),
        _.putDouble( _, _ )
    )

    implicit val `Bundleize[IBinder]` = new Bundleize[IBinder] {
        @TargetApi( 18 )
        override def read( key: String, bundle: Bundle ) = Option( bundle.getBinder( key ) ).getOrElse( bailOut( key ) )

        @TargetApi( 18 )
        override def write( key: String, value: IBinder, bundle: Bundle ) = bundle.putBinder( key, value )
    }

    implicit val `Bundleize[Float]` = Bundleize[Float](
        ( bundle, key ) ⇒ bundle.getFloat( key, bailOut( key ) ),
        _.putFloat( _, _ )
    )

    implicit val `Bundleize[Int]` = Bundleize[Int](
        ( bundle, key ) ⇒ bundle.getInt( key, bailOut( key ) ),
        _.putInt( _, _ )
    )

    implicit val `Bundleize[Long]` = Bundleize[Long](
        ( bundle, key ) ⇒ bundle.getLong( key, bailOut( key ) ),
        _.putLong( _, _ )
    )

    implicit def `Bundleize[Parcelable]`[T <: android.os.Parcelable] = Bundleize[T](
        ( bundle, key ) ⇒ Option( bundle.getParcelable[T]( key ) ).getOrElse( bailOut( key ) ),
        _.putParcelable( _, _ )
    )

    implicit val `Bundleize[Short]` = Bundleize[Short](
        ( bundle, key ) ⇒ bundle.getShort( key, bailOut( key ) ),
        _.putShort( _, _ )
    )

    implicit val `Bundleize[Size]` = new Bundleize[Size] {
        @TargetApi( 21 )
        override def read( key: String, bundle: Bundle ) = Option( bundle.getSize( key ) ).getOrElse( bailOut( key ) )

        @TargetApi( 21 )
        override def write( key: String, value: Size, bundle: Bundle ) = bundle.putSize( key, value )
    }

    implicit val `Bundleize[SizeF]` = new Bundleize[SizeF] {
        @TargetApi( 21 )
        override def read( key: String, bundle: Bundle ) = Option( bundle.getSizeF( key ) ).getOrElse( bailOut( key ) )

        @TargetApi( 21 )
        override def write( key: String, value: SizeF, bundle: Bundle ) = bundle.putSizeF( key, value )
    }

    implicit val `Bundleize[String]` = Bundleize[String](
        ( bundle, key ) ⇒ bundle.getString( key, bailOut( key ) ),
        _.putString( _, _ )
    )

    implicit def `Bundleize[Option]`[T: Bundleize] = new Bundleize[Option[T]] {
        override def read( key: String, bundle: Bundle ) = {
            bundle.get( key ) match {
                case bundle: Bundle if bundle.containsKey( "option" ) ⇒
                    bundle.read[Int]( "option" ) match {
                        case 1  ⇒ Some( bundle.read[T]( "value" ) )
                        case -1 ⇒ None
                    }
                case null ⇒ None
                case _    ⇒ Try( Some( bundle.read[T]( key ) ) ).getOrElse( None )
            }
        }

        override def write( key: String, value: Option[T], bundle: Bundle ) = {
            val nested = value match {
                case Some( value ) ⇒
                    new Bundle( 2 )
                        .write( "option", 1 )
                        .write( "value", value )
                case None ⇒ new Bundle( 1 ).write( "option", -1 )
            }

            bundle.write( key, nested )
        }
    }

    implicit def `Bundleize[Either]`[A: Bundleize, B: Bundleize] = new Bundleize[Either[A, B]] {
        override def read( key: String, bundle: Bundle ) = {
            val nested = bundle.read[Bundle]( key )

            nested.read[Int]( "either" ) match {
                case -1 ⇒ Left( nested.read[A]( "value" ) )
                case 1  ⇒ Right( nested.read[B]( "value" ) )
            }
        }

        override def write( key: String, value: Either[A, B], bundle: Bundle ) = {
            val nested = new Bundle( 2 )

            value match {
                case Left( value ) ⇒
                    nested.write( "either", -1 )
                    nested.write( "value", value )
                case Right( value ) ⇒
                    nested.write( "either", 1 )
                    nested.write( "value", value )
            }

            bundle.write( key, nested )
        }
    }

    implicit def `Bundleize[Traversable]`[L[B] <: Traversable[B], T: Bundleize: ClassTag]( implicit cbf: CanBuildFrom[Nothing, T, L[T]] ) = {
        new Bundleize[L[T]] {
            override def read( key: String, bundle: Bundle ) = `Bundleize[Array]`[T].read( key, bundle ).to[L]

            override def write( key: String, value: L[T], bundle: Bundle ) = {
                `Bundleize[Array]`[T].write( key, value.toArray, bundle )
            }
        }
    }

    implicit def `Bundleize[Array]`[T: Bundleize: ClassTag] = new Bundleize[Array[T]] {
        val bundleize = implicitly[Bundleize[T]]

        override def read( key: String, bundle: Bundle ) = {
            def get[S]( array: Array[S] ) = Option( array.asInstanceOf[Array[T]] ).getOrElse( bailOut( key ) )

            classTag[T].runtimeClass match {
                case tag if tag == classOf[Boolean] ⇒ get( bundle.getBooleanArray( key ) )
                case tag if tag == classOf[Byte]    ⇒ get( bundle.getByteArray( key ) )
                case tag if tag == classOf[Char]    ⇒ get( bundle.getCharArray( key ) )
                case tag if tag == classOf[Double]  ⇒ get( bundle.getDoubleArray( key ) )
                case tag if tag == classOf[Float]   ⇒ get( bundle.getFloatArray( key ) )
                case tag if tag == classOf[Int]     ⇒ get( bundle.getIntArray( key ) )
                case tag if tag == classOf[Long]    ⇒ get( bundle.getLongArray( key ) )
                case tag if tag == classOf[Short]   ⇒ get( bundle.getShortArray( key ) )
                case tag if tag == classOf[String]  ⇒ get( bundle.getStringArray( key ) )
                case tag if classOf[android.os.Parcelable].isAssignableFrom( tag ) ⇒
                    get( bundle.getParcelableArray( key ) )
                case _ ⇒
                    import scala.collection.JavaConversions._

                    val nested = bundle.read[Bundle]( key )
                    nested.keySet().map( bundleize.read( _, nested ) )( breakOut )
            }
        }

        override def write( key: String, value: Array[T], bundle: Bundle ) = {
            def put[S]( f: Array[S] ⇒ Unit ) = f( value.asInstanceOf[Array[S]] )

            classTag[T].runtimeClass match {
                case tag if tag == classOf[Boolean] ⇒ put[Boolean]( bundle.putBooleanArray( key, _ ) )
                case tag if tag == classOf[Byte]    ⇒ put[Byte]( bundle.putByteArray( key, _ ) )
                case tag if tag == classOf[Char]    ⇒ put[Char]( bundle.putCharArray( key, _ ) )
                case tag if tag == classOf[Double]  ⇒ put[Double]( bundle.putDoubleArray( key, _ ) )
                case tag if tag == classOf[Float]   ⇒ put[Float]( bundle.putFloatArray( key, _ ) )
                case tag if tag == classOf[Int]     ⇒ put[Int]( bundle.putIntArray( key, _ ) )
                case tag if tag == classOf[Long]    ⇒ put[Long]( bundle.putLongArray( key, _ ) )
                case tag if tag == classOf[Short]   ⇒ put[Short]( bundle.putShortArray( key, _ ) )
                case tag if tag == classOf[String]  ⇒ put[String]( bundle.putStringArray( key, _ ) )
                case tag if classOf[android.os.Parcelable].isAssignableFrom( tag ) ⇒
                    put[android.os.Parcelable]( bundle.putParcelableArray( key, _ ) )
                case _ ⇒
                    val array = value.zipWithIndex
                    val nested = new Bundle( array.length )

                    array.foreach { case ( value, index ) ⇒ bundleize.write( index.toString, value, nested ) }
                    bundle.write( key, nested )
            }
        }
    }
}