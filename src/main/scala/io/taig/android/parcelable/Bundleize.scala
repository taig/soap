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
        override def read( key: String, bundle: Bundle ) = {
            if ( bundle.containsKey( key ) ) {
                r( bundle, key )
            } else {
                throw new IllegalStateException(
                    s"Key '$key' does not exist, use Option instead or get your shit together"
                )
            }
        }

        override def write( key: String, value: T, bundle: Bundle ) = w( bundle, key, value )
    }

    implicit val `Bundleize[Boolean]` = Bundleize[Boolean]( _.getBoolean( _ ), _.putBoolean( _, _ ) )

    implicit val `Bundleize[Bundle]` = Bundleize[Bundle]( _.getBundle( _ ), _.putBundle( _, _ ) )

    implicit val `Bundleize[Byte]` = Bundleize[Byte]( _.getByte( _ ), _.putByte( _, _ ) )

    implicit val `Bundleize[Char]` = Bundleize[Char]( _.getChar( _ ), _.putChar( _, _ ) )

    implicit val `Bundleize[CharSequence]` = Bundleize[CharSequence](
        _.getCharSequence( _ ),
        _.putCharSequence( _, _ )
    )

    implicit val `Bundleize[Double]` = Bundleize[Double]( _.getDouble( _ ), _.putDouble( _, _ ) )

    implicit val `Bundleize[IBinder]` = new Bundleize[IBinder] {
        @TargetApi( 18 )
        override def read( key: String, bundle: Bundle ) = bundle.getBinder( key )

        @TargetApi( 18 )
        override def write( key: String, value: IBinder, bundle: Bundle ) = bundle.putBinder( key, value )
    }

    implicit val `Bundleize[Float]` = Bundleize[Float]( _.getFloat( _ ), _.putFloat( _, _ ) )

    implicit val `Bundleize[Int]` = Bundleize[Int]( _.getInt( _ ), _.putInt( _, _ ) )

    implicit val `Bundleize[Long]` = Bundleize[Long]( _.getLong( _ ), _.putLong( _, _ ) )

    implicit def `Bundleize[Parcelable]`[T <: android.os.Parcelable] = Bundleize[T](
        _.getParcelable[T]( _ ),
        _.putParcelable( _, _ )
    )

    implicit val `Bundleize[Short]` = Bundleize[Short]( _.getShort( _ ), _.putShort( _, _ ) )

    implicit val `Bundleize[Size]` = new Bundleize[Size] {
        @TargetApi( 21 )
        override def read( key: String, bundle: Bundle ) = bundle.getSize( key )

        @TargetApi( 21 )
        override def write( key: String, value: Size, bundle: Bundle ) = bundle.putSize( key, value )
    }

    implicit val `Bundleize[SizeF]` = new Bundleize[SizeF] {
        @TargetApi( 21 )
        override def read( key: String, bundle: Bundle ) = bundle.getSizeF( key )

        @TargetApi( 21 )
        override def write( key: String, value: SizeF, bundle: Bundle ) = bundle.putSizeF( key, value )
    }

    implicit val `Bundleize[String]` = Bundleize[String]( _.getString( _ ), _.putString( _, _ ) )

    implicit def `Bundleize[Option]`[T: Bundleize] = new Bundleize[Option[T]] {
        override def read( key: String, bundle: Bundle ) = {
            if ( bundle.containsKey( key ) ) {
                bundle.get( key ) match {
                    case bundle: Bundle if bundle.containsKey( "option" ) ⇒
                        bundle.read[Int]( "option" ) match {
                            case 1  ⇒ Some( bundle.read[T]( "value" ) )
                            case -1 ⇒ None
                        }
                    case null ⇒ None
                    case _    ⇒ Try( Some( bundle.read[T]( key ) ) ).getOrElse( None )
                }
            } else {
                None
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
            def get[S]( array: Array[S] ) = array.asInstanceOf[Array[T]]

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