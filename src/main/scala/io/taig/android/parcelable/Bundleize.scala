package io.taig.android.parcelable

import android.annotation.TargetApi
import android.os.{ Parcelable, IBinder, Bundle }
import android.util.{ SizeF, Size }

import scala.collection._
import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import scala.reflect.ClassTag

trait Bundleize[T] {
    def get( key: String, bundle: Bundle ): T

    def put( key: String, value: T, bundle: Bundle ): Unit
}

object Bundleize {
    def apply[T]( g: ( Bundle, String ) ⇒ T, p: ( Bundle, String, T ) ⇒ Unit ): Bundleize[T] = new Bundleize[T] {
        override def get( key: String, bundle: Bundle ) = g( bundle, key )

        override def put( key: String, value: T, bundle: Bundle ) = p( bundle, key, value )
    }

    implicit val `Bundleize[Boolean]` = Bundleize[Boolean]( _.getBoolean( _ ), _.putBoolean( _, _ ) )

    implicit val `Bundleize[Bundle]` = Bundleize[Bundle]( _.getBundle( _ ), _.putBundle( _, _ ) )

    implicit val `Bundleize[Byte]` = Bundleize[Byte]( _.getByte( _ ), _.putByte( _, _ ) )

    implicit val `Bundleize[Char]` = Bundleize[Char]( _.getChar( _ ), _.putChar( _, _ ) )

    implicit val `Bundleize[CharSequence]` = Bundleize[CharSequence]( _.getCharSequence( _ ), _.putCharSequence( _, _ ) )

    implicit val `Bundleize[Double]` = Bundleize[Double]( _.getDouble( _ ), _.putDouble( _, _ ) )

    implicit val `Bundleize[IBinder]` = new Bundleize[IBinder] {
        @TargetApi( 18 )
        override def get( key: String, bundle: Bundle ) = bundle.getBinder( key )

        @TargetApi( 18 )
        override def put( key: String, value: IBinder, bundle: Bundle ) = bundle.putBinder( key, value )
    }

    implicit val `Bundleize[Float]` = Bundleize[Float]( _.getFloat( _ ), _.putFloat( _, _ ) )

    implicit val `Bundleize[Int]` = Bundleize[Int]( _.getInt( _ ), _.putInt( _, _ ) )

    implicit val `Bundleize[Long]` = Bundleize[Long]( _.getLong( _ ), _.putLong( _, _ ) )

    implicit def `Bundleize[Parcelable]`[T <: Parcelable] = {
        Bundleize[T]( _.getParcelable[T]( _ ), _.putParcelable( _, _ ) )
    }

    implicit val `Bundleize[Short]` = Bundleize[Short]( _.getShort( _ ), _.putShort( _, _ ) )

    implicit val `Bundleize[Size]` = new Bundleize[Size] {
        @TargetApi( 21 )
        override def get( key: String, bundle: Bundle ) = bundle.getSize( key )

        @TargetApi( 21 )
        override def put( key: String, value: Size, bundle: Bundle ) = bundle.putSize( key, value )
    }

    implicit val `Bundleize[SizeF]` = new Bundleize[SizeF] {
        @TargetApi( 21 )
        override def get( key: String, bundle: Bundle ) = bundle.getSizeF( key )

        @TargetApi( 21 )
        override def put( key: String, value: SizeF, bundle: Bundle ) = bundle.putSizeF( key, value )
    }

    implicit val `Bundleize[String]` = Bundleize[String]( _.getString( _ ), _.putString( _, _ ) )

    implicit def `Bundleize[Option]`[T: Bundleize] = new Bundleize[Option[T]] {
        val bundleize = implicitly[Bundleize[T]]

        override def get( key: String, bundle: Bundle ) = Option( bundleize.get( key, bundle ) )

        override def put( key: String, value: Option[T], bundle: Bundle ) = {
            bundleize.put( key, value.getOrElse( null.asInstanceOf[T] ), bundle )
        }
    }

    implicit def `Bundleize[Traversable]`[L[B] <: Traversable[B], T: Bundleize]( implicit cbf: CanBuildFrom[Nothing, T, L[T]] ) = {
        import scala.collection.JavaConversions._

        new Bundleize[L[T]] {
            val bundleize = implicitly[Bundleize[T]]

            override def get( key: String, bundle: Bundle ) = {
                val listBundle = bundle.getBundle( key )
                listBundle.keySet().map( i ⇒ bundleize.get( i, listBundle ) )( breakOut )
            }

            override def put( key: String, value: L[T], bundle: Bundle ) = {
                val seq = value.toSeq.zipWithIndex
                val listBundle = new Bundle( seq.length )

                seq.foreach { case ( value, index ) ⇒ bundleize.put( index.toString, value, listBundle ) }
                bundle.putAll( listBundle )
            }
        }
    }

    implicit def `Bundleize[Array]`[T: Bundleize: ClassTag] = new Bundleize[Array[T]] {
        override def get( key: String, bundle: Bundle ) = `Bundleize[Traversable]`[Seq, T].get( key, bundle ).toArray

        override def put( key: String, value: Array[T], bundle: Bundle ) = {
            `Bundleize[Traversable]`[Seq, T].put( key, value.toSeq, bundle )
        }
    }
}