package io.taig.android.parcelable

import java.util

import android.annotation.TargetApi
import android.os.IBinder
import android.util.{ Size, SizeF }
import shapeless._
import shapeless.syntax.singleton._

import scala.collection.breakOut
import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import scala.reflect._
import scala.util.Try

/**
 * Type class that instructs how to read/write a value from/to a given Bundle
 */
object Bundleize {
    trait Read[T] {
        def read( bundle: Bundle, key: String ): T
    }

    trait LowPriorityRead {
        implicit def `Read[Array]`[T: Read: ClassTag]: Read[Array[T]] = new Read[Array[T]] {
            override def read( bundle: Bundle, key: String ) = {
                import collection.JavaConversions._

                val nested = bundle.read[Bundle]( key )
                nested.keySet().map( implicitly[Read[T]].read( nested, _ ) )( breakOut )
            }
        }

        implicit def `Read[Traversable]`[L[B] <: Traversable[B], T: Read: ClassTag](
            implicit
            cbf: CanBuildFrom[Nothing, T, L[T]]
        ): Read[L[T]] = new Read[L[T]] {
            override def read( bundle: Bundle, key: String ) = `Read[Array]`[T].read( bundle, key ).to[L]
        }
    }

    object Read extends LowPriorityRead {
        def apply[T]( f: ( Bundle, String ) ⇒ T ) = new Read[T] {
            override def read( bundle: Bundle, key: String ) = bundle.checked( key )( f )
        }

        implicit val `Read[Array[Boolean]`: Read[Array[Boolean]] = Read( _.getBooleanArray( _ ) )

        implicit val `Read[Array[Byte]`: Read[Array[Byte]] = Read( _.getByteArray( _ ) )

        implicit val `Read[Array[Char]`: Read[Array[Char]] = Read( _.getCharArray( _ ) )

        implicit val `Read[Array[Double]`: Read[Array[Double]] = Read( _.getDoubleArray( _ ) )

        implicit val `Read[Array[Float]`: Read[Array[Float]] = Read( _.getFloatArray( _ ) )

        implicit val `Read[Array[Int]`: Read[Array[Int]] = Read( _.getIntArray( _ ) )

        implicit val `Read[Array[Long]`: Read[Array[Long]] = Read( _.getLongArray( _ ) )

        implicit val `Read[Array[Parcelable]`: Read[Array[android.os.Parcelable]] = Read( _.getParcelableArray( _ ) )

        implicit val `Read[Array[Short]`: Read[Array[Short]] = Read( _.getShortArray( _ ) )

        implicit val `Read[Array[String]`: Read[Array[String]] = Read( _.getStringArray( _ ) )

        implicit val `Read[Boolean]`: Read[Boolean] = Read( _.getBoolean( _ ) )

        implicit val `Read[Bundle]`: Read[Bundle] = Read( _.getBundle( _ ) )

        implicit val `Read[Byte]`: Read[Byte] = Read( _.getByte( _ ) )

        implicit val `Read[Char]`: Read[Char] = Read( _.getChar( _ ) )

        implicit val `Read[CharSequence]`: Read[CharSequence] = Read( _.getCharSequence( _ ) )

        implicit val `Read[Double]`: Read[Double] = Read( _.getDouble( _ ) )

        implicit def `Read[Either]`[A: Read, B: Read]: Read[Either[A, B]] = new Read[Either[A, B]] {
            override def read( bundle: Bundle, key: String ) = {
                val nested = bundle.read[Bundle]( key )

                nested.read[Int]( "either" ) match {
                    case -1 ⇒ Left( nested.read[A]( "value" ) )
                    case 1  ⇒ Right( nested.read[B]( "value" ) )
                }
            }
        }

        implicit val `Read[IBinder]`: Read[IBinder] = new Read[IBinder] {
            @TargetApi( 18 )
            override def read( bundle: Bundle, key: String ) = bundle.checked( key )( _.getBinder( _ ) )
        }

        implicit val `Read[Float]`: Read[Float] = Read( _.getFloat( _ ) )

        implicit val `Read[Int]`: Read[Int] = Read( _.getInt( _ ) )

        implicit val `Read[Long]`: Read[Long] = Read( _.getLong( _ ) )

        implicit def `Read[Option]`[T: Read]: Read[Option[T]] = new Read[Option[T]] {
            override def read( bundle: Bundle, key: String ) = {
                if ( bundle.containsKey( key ) ) {
                    bundle.get( key ) match {
                        case bundle: Bundle if bundle.containsKey( "option" ) ⇒ bundle.read[Int]( "option" ) match {
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
        }

        implicit def `Read[Parcelable]`[T <: android.os.Parcelable]: Read[T] = Read[T]( _.getParcelable[T]( _ ) )

        implicit val `Read[Short]`: Read[Short] = Read( _.getShort( _ ) )

        implicit val `Read[Size]`: Read[Size] = new Read[Size] {
            @TargetApi( 21 )
            override def read( bundle: Bundle, key: String ) = bundle.checked( key )( _.getSize( _ ) )
        }

        implicit val `Read[SizeF]`: Read[SizeF] = new Read[SizeF] {
            @TargetApi( 21 )
            override def read( bundle: Bundle, key: String ) = bundle.checked( key )( _.getSizeF( _ ) )
        }

        implicit val `Read[String]`: Read[String] = Read( _.getString( _ ) )

        implicit def `Read[Traversable[Boolean]]`[L[B] <: Traversable[B]](
            implicit
            cbf: CanBuildFrom[Nothing, Boolean, L[Boolean]]
        ): Read[L[Boolean]] = {
            new Read[L[Boolean]] {
                override def read( bundle: Bundle, key: String ) = `Read[Array[Boolean]`.read( bundle, key ).to[L]
            }
        }

        implicit def `Read[Traversable[Byte]]`[L[B] <: Traversable[B]](
            implicit
            cbf: CanBuildFrom[Nothing, Byte, L[Byte]]
        ): Read[L[Byte]] = {
            new Read[L[Byte]] {
                override def read( bundle: Bundle, key: String ) = `Read[Array[Byte]`.read( bundle, key ).to[L]
            }
        }

        implicit def `Read[Traversable[Char]]`[L[B] <: Traversable[B]](
            implicit
            cbf: CanBuildFrom[Nothing, Char, L[Char]]
        ): Read[L[Char]] = {
            new Read[L[Char]] {
                override def read( bundle: Bundle, key: String ) = `Read[Array[Char]`.read( bundle, key ).to[L]
            }
        }

        implicit def `Read[Traversable[Double]]`[L[B] <: Traversable[B]](
            implicit
            cbf: CanBuildFrom[Nothing, Double, L[Double]]
        ): Read[L[Double]] = {
            new Read[L[Double]] {
                override def read( bundle: Bundle, key: String ) = `Read[Array[Double]`.read( bundle, key ).to[L]
            }
        }

        implicit def `Read[Traversable[Float]]`[L[B] <: Traversable[B]](
            implicit
            cbf: CanBuildFrom[Nothing, Float, L[Float]]
        ): Read[L[Float]] = {
            new Read[L[Float]] {
                override def read( bundle: Bundle, key: String ) = `Read[Array[Float]`.read( bundle, key ).to[L]
            }
        }

        implicit def `Read[Traversable[Int]]`[L[B] <: Traversable[B]](
            implicit
            cbf: CanBuildFrom[Nothing, Int, L[Int]]
        ): Read[L[Int]] = {
            new Read[L[Int]] {
                override def read( bundle: Bundle, key: String ) = `Read[Array[Int]`.read( bundle, key ).to[L]
            }
        }

        implicit def `Read[Traversable[Long]]`[L[B] <: Traversable[B]](
            implicit
            cbf: CanBuildFrom[Nothing, Long, L[Long]]
        ): Read[L[Long]] = {
            new Read[L[Long]] {
                override def read( bundle: Bundle, key: String ) = `Read[Array[Long]`.read( bundle, key ).to[L]
            }
        }

        implicit def `Read[Traversable[Parcelable]]`[L[B] <: Traversable[B]](
            implicit
            cbf: CanBuildFrom[Nothing, android.os.Parcelable, L[android.os.Parcelable]]
        ): Read[L[android.os.Parcelable]] = {
            new Read[L[android.os.Parcelable]] {
                override def read( bundle: Bundle, key: String ) = `Read[Array[Parcelable]`.read( bundle, key ).to[L]
            }
        }

        implicit def `Read[Traversable[Short]]`[L[B] <: Traversable[B]](
            implicit
            cbf: CanBuildFrom[Nothing, Short, L[Short]]
        ): Read[L[Short]] = {
            new Read[L[Short]] {
                override def read( bundle: Bundle, key: String ) = `Read[Array[Short]`.read( bundle, key ).to[L]
            }
        }

        implicit def `Read[Traversable[String]]`[L[B] <: Traversable[B]](
            implicit
            cbf: CanBuildFrom[Nothing, String, L[String]]
        ): Read[L[String]] = {
            new Read[L[String]] {
                override def read( bundle: Bundle, key: String ) = `Read[Array[String]`.read( bundle, key ).to[L]
            }
        }
    }

    trait Write[-T] {
        def write( bundle: Bundle, key: String, value: T ): Unit
    }

    trait LowPriorityWrite {
        implicit def `Write[Array]`[T: Write]: Write[Array[T]] = new Write[Array[T]] {
            override def write( bundle: Bundle, key: String, value: Array[T] ) = {
                val array = value.zipWithIndex
                val nested = Bundle( array.length )
                array.foreach{ case ( value, index ) ⇒ nested.write( index.toString, value ) }
                bundle.write( key, nested )
            }
        }

        implicit def `Write[Traversable]`[L[B] <: Traversable[B], T: Write: ClassTag]: Write[L[T]] = new Write[L[T]] {
            override def write( bundle: Bundle, key: String, value: L[T] ) = {
                `Write[Array]`[T].write( bundle, key, value.toArray )
            }
        }
    }

    object Write extends LowPriorityWrite {
        def apply[T]( f: ( Bundle, String, T ) ⇒ Unit ) = new Write[T] {
            override def write( bundle: Bundle, key: String, value: T ) = f( bundle, key, value )
        }

        implicit val `Write[Array[Boolean]]`: Write[Array[Boolean]] = Write( _.putBooleanArray( _, _ ) )

        implicit val `Write[Array[Byte]]`: Write[Array[Byte]] = Write( _.putByteArray( _, _ ) )

        implicit val `Write[Array[Char]]`: Write[Array[Char]] = Write( _.putCharArray( _, _ ) )

        implicit val `Write[Array[Double]]`: Write[Array[Double]] = Write( _.putDoubleArray( _, _ ) )

        implicit val `Write[Array[Float]]`: Write[Array[Float]] = Write( _.putFloatArray( _, _ ) )

        implicit val `Write[Array[Int]]`: Write[Array[Int]] = Write( _.putIntArray( _, _ ) )

        implicit val `Write[Array[Long]]`: Write[Array[Long]] = Write( _.putLongArray( _, _ ) )

        implicit val `Write[Array[Parcelable]]`: Write[Array[android.os.Parcelable]] = {
            Write( _.putParcelableArray( _, _ ) )
        }

        implicit val `Write[Array[Short]]`: Write[Array[Short]] = Write( _.putShortArray( _, _ ) )

        implicit val `Write[Array[String]]`: Write[Array[String]] = Write( _.putStringArray( _, _ ) )

        implicit val `Write[Boolean]`: Write[Boolean] = Write( _.putBoolean( _, _ ) )

        implicit val `Write[Bundle]`: Write[Bundle] = Write( _.putBundle( _, _ ) )

        implicit val `Write[Byte]`: Write[Byte] = Write( _.putByte( _, _ ) )

        implicit val `Write[Char]`: Write[Char] = Write( _.putChar( _, _ ) )

        implicit val `Write[CharSequence]`: Write[CharSequence] = new Write[CharSequence] {
            override def write( bundle: Bundle, key: String, value: CharSequence ) = value match {
                case value: String ⇒ bundle.putString( key, value )
                case _             ⇒ bundle.putCharSequence( key, value )
            }
        }

        implicit val `Write[Double]`: Write[Double] = Write( _.putDouble( _, _ ) )

        implicit def `Write[Either]`[A: Write, B: Write]: Write[Either[A, B]] = new Write[Either[A, B]] {
            override def write( bundle: Bundle, key: String, value: Either[A, B] ) = value match {
                case left @ Left( _ )   ⇒ `Write[Left]`[A].write( bundle, key, left )
                case right @ Right( _ ) ⇒ `Write[Right]`[B].write( bundle, key, right )
            }
        }

        implicit val `Write[IBinder]`: Write[IBinder] = new Write[IBinder] {
            @TargetApi( 18 )
            override def write( bundle: Bundle, key: String, value: IBinder ) = bundle.putBinder( key, value )
        }

        implicit val `Write[Float]`: Write[Float] = Write( _.putFloat( _, _ ) )

        implicit val `Write[Int]`: Write[Int] = Write( _.putInt( _, _ ) )

        implicit def `Write[Left]`[A: Write]: Write[Left[A, _]] = new Write[Left[A, _]] {
            override def write( bundle: Bundle, key: String, value: Left[A, _] ) = {
                bundle.write( key, Bundle( "either" ->> -1 :: "value" ->> value.a :: HNil ) )
            }
        }

        implicit val `Write[Long]`: Write[Long] = Write( _.putLong( _, _ ) )

        implicit val `Write[None]`: Write[None.type] = new Write[None.type] {
            override def write( bundle: Bundle, key: String, value: None.type ) = {
                bundle.write( key, Bundle( "option" ->> -1 :: HNil ) )
            }
        }

        implicit def `Write[Option]`[T: Write]: Write[Option[T]] = new Write[Option[T]] {
            override def write( bundle: Bundle, key: String, value: Option[T] ) = value match {
                case Some( value ) ⇒ bundle.write( key, Bundle( "option" ->> 1 :: "value" ->> value :: HNil ) )
                case None          ⇒ bundle.write( key, Bundle( "option" ->> -1 :: HNil ) )
            }
        }

        implicit val `Write[Parcelable]`: Write[android.os.Parcelable] = {
            Write[android.os.Parcelable]( _.putParcelable( _, _ ) )
        }

        implicit def `Write[Right]`[B: Write]: Write[Right[_, B]] = new Write[Right[_, B]] {
            override def write( bundle: Bundle, key: String, value: Right[_, B] ) = {
                bundle.write( key, Bundle( "either" ->> 1 :: "value" ->> value.b :: HNil ) )
            }
        }

        implicit val `Write[Short]`: Write[Short] = Write( _.putShort( _, _ ) )

        implicit val `Write[Size]`: Write[Size] = new Write[Size] {
            @TargetApi( 21 )
            override def write( bundle: Bundle, key: String, value: Size ) = bundle.putSize( key, value )
        }

        implicit val `Write[SizeF]`: Write[SizeF] = new Write[SizeF] {
            @TargetApi( 21 )
            override def write( bundle: Bundle, key: String, value: SizeF ) = bundle.putSizeF( key, value )
        }

        implicit def `Write[Traversable[Boolean]]`[L <: Traversable[Boolean]]: Write[Traversable[Boolean]] = {
            new Write[Traversable[Boolean]] {
                override def write( bundle: Bundle, key: String, value: Traversable[Boolean] ) = {
                    `Write[Array[Boolean]]`.write( bundle, key, value.toArray )
                }
            }
        }

        implicit def `Write[Traversable[Byte]]`[L <: Traversable[Byte]]: Write[Traversable[Byte]] = {
            new Write[Traversable[Byte]] {
                override def write( bundle: Bundle, key: String, value: Traversable[Byte] ) = {
                    `Write[Array[Byte]]`.write( bundle, key, value.toArray )
                }
            }
        }

        implicit def `Write[Traversable[Char]]`[L <: Traversable[Char]]: Write[Traversable[Char]] = {
            new Write[Traversable[Char]] {
                override def write( bundle: Bundle, key: String, value: Traversable[Char] ) = {
                    `Write[Array[Char]]`.write( bundle, key, value.toArray )
                }
            }
        }

        implicit def `Write[Traversable[Double]]`[L <: Traversable[Double]]: Write[Traversable[Double]] = {
            new Write[Traversable[Double]] {
                override def write( bundle: Bundle, key: String, value: Traversable[Double] ) = {
                    `Write[Array[Double]]`.write( bundle, key, value.toArray )
                }
            }
        }

        implicit def `Write[Traversable[Float]]`[L <: Traversable[Float]]: Write[Traversable[Float]] = {
            new Write[Traversable[Float]] {
                override def write( bundle: Bundle, key: String, value: Traversable[Float] ) = {
                    `Write[Array[Float]]`.write( bundle, key, value.toArray )
                }
            }
        }

        implicit def `Write[Traversable[Int]]`[L <: Traversable[Int]]: Write[Traversable[Int]] = {
            new Write[Traversable[Int]] {
                override def write( bundle: Bundle, key: String, value: Traversable[Int] ) = {
                    `Write[Array[Int]]`.write( bundle, key, value.toArray )
                }
            }
        }

        implicit def `Write[Traversable[Long]]`[L <: Traversable[Long]]: Write[Traversable[Long]] = {
            new Write[Traversable[Long]] {
                override def write( bundle: Bundle, key: String, value: Traversable[Long] ) = {
                    `Write[Array[Long]]`.write( bundle, key, value.toArray )
                }
            }
        }

        implicit def `Write[Traversable[Parcelable]]`[L <: Traversable[android.os.Parcelable]]: Write[Traversable[android.os.Parcelable]] = {
            new Write[Traversable[android.os.Parcelable]] {
                override def write( bundle: Bundle, key: String, value: Traversable[android.os.Parcelable] ) = {
                    `Write[Array[Parcelable]]`.write( bundle, key, value.toArray )
                }
            }
        }

        implicit def `Write[Traversable[Short]]`[L <: Traversable[Short]]: Write[Traversable[Short]] = {
            new Write[Traversable[Short]] {
                override def write( bundle: Bundle, key: String, value: Traversable[Short] ) = {
                    `Write[Array[Short]]`.write( bundle, key, value.toArray )
                }
            }
        }

        implicit def `Write[Traversable[String]]`[L <: Traversable[String]]: Write[Traversable[String]] = {
            new Write[Traversable[String]] {
                override def write( bundle: Bundle, key: String, value: Traversable[String] ) = {
                    `Write[Array[String]]`.write( bundle, key, value.toArray )
                }
            }
        }
    }
}