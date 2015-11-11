package io.taig.android.parcelable

import java.io.FileDescriptor
import java.net.URL

import android.annotation.TargetApi
import android.os._
import android.text.TextUtils
import android.util.{ Size, SizeF, SparseBooleanArray }

/**
 * Type class that instructs how to read/write a value from/to a given Parcel
 */
object Parcelize {
    trait Read[T] {
        def read( source: Parcel ): T
    }

    object Read {
        def apply[T]( f: Parcel ⇒ T ): Read[T] = new Read[T] {
            override def read( source: Parcel ) = f( source )
        }

        implicit val `Read[Bundle]`: Read[Bundle] = Read( _.readBundle )

        implicit val `Read[Boolean]`: Read[Boolean] = Read( _.readValue( null ).asInstanceOf[Boolean] )

        implicit val `Read[Byte]`: Read[Byte] = Read( _.readByte )

        implicit val `Read[Char]`: Read[Char] = Read( _.readInt.toChar )

        implicit val `Read[CharSequence]`: Read[CharSequence] = Read[CharSequence] {
            TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel
        }

        implicit val `Read[Double]`: Read[Double] = Read[Double]( _.readDouble )

        implicit val `Read[FileDescriptor]`: Read[FileDescriptor] = Read( _.readFileDescriptor.getFileDescriptor )

        implicit val `Read[Float]`: Read[Float] = Read( _.readFloat )

        implicit val `Read[IBinder]`: Read[IBinder] = Read( _.readStrongBinder )

        implicit val `Read[Int]`: Read[Int] = Read( _.readInt )

        implicit val `Read[Long]`: Read[Long] = Read( _.readLong )

        implicit val `Read[PersistableBundle]`: Read[PersistableBundle] = new Read[PersistableBundle] {
            @TargetApi( 21 )
            override def read( source: Parcel ) = source.readPersistableBundle()
        }

        implicit val `Read[Short]`: Read[Short] = Read( _.readValue( null ).asInstanceOf[Short] )

        implicit val `Read[Size]`: Read[Size] = new Read[Size] {
            @TargetApi( 21 )
            override def read( source: Parcel ) = source.readSize()
        }

        implicit val `Read[SizeF]`: Read[SizeF] = new Read[SizeF] {
            @TargetApi( 21 )
            override def read( source: Parcel ) = source.readSizeF()
        }

        implicit val `Read[SparceBooleanArray]`: Read[SparseBooleanArray] = Read( _.readSparseBooleanArray )

        implicit val `Read[String]`: Read[String] = Read( _.readString )

        implicit val `Read[URL]`: Read[URL] = Read[URL]( source ⇒ new URL( source.read[String] ) )
    }

    trait Write[-T] {
        def write( destination: Parcel, value: T, flags: Int ): Unit
    }

    object Write {
        def apply[T]( f: ( Parcel, T ) ⇒ Unit ): Write[T] = new Write[T] {
            def write( destination: Parcel, value: T, flags: Int ) = f( destination, value )
        }

        implicit val `Write[Bundle]`: Write[Bundle] = Write( _.writeBundle( _ ) )

        implicit val `Write[Boolean]`: Write[Boolean] = Write( _.writeValue( _ ) )

        implicit val `Write[Byte]`: Write[Byte] = Write( _.writeByte( _ ) )

        implicit val `Write[Char]`: Write[Char] = Write { ( destination, value ) ⇒
            destination.writeInt( value.toInt )
        }

        implicit val `Write[CharSequence]`: Write[CharSequence] = new Write[CharSequence] {
            override def write( destination: Parcel, value: CharSequence, flags: Int ) = {
                TextUtils.writeToParcel( value, destination, flags )
            }
        }

        implicit val `Write[Double]`: Write[Double] = Write[Double]( _.writeDouble( _ ) )

        implicit val `Write[FileDescriptor]`: Write[FileDescriptor] = Write( _.writeFileDescriptor( _ ) )

        implicit val `Write[Float]`: Write[Float] = Write( _.writeFloat( _ ) )

        implicit val `Write[IBinder]`: Write[IBinder] = Write( _.writeStrongBinder( _ ) )

        implicit val `Write[Int]`: Write[Int] = Write( _.writeInt( _ ) )

        implicit val `Write[Long]`: Write[Long] = Write( _.writeLong( _ ) )

        implicit val `Write[PersistableBundle]`: Write[PersistableBundle] = new Write[PersistableBundle] {
            @TargetApi( 21 )
            override def write( destination: Parcel, value: PersistableBundle, flags: Int ) = {
                destination.writePersistableBundle( value )
            }
        }

        implicit val `Write[Short]`: Write[Short] = Write( _.writeValue( _ ) )

        implicit val `Write[Size]`: Write[Size] = new Write[Size] {
            @TargetApi( 21 )
            override def write( destination: Parcel, value: Size, flags: Int ) = destination.writeSize( value )
        }

        implicit val `Write[SizeF]`: Write[SizeF] = new Write[SizeF] {
            @TargetApi( 21 )
            override def write( destination: Parcel, value: SizeF, flags: Int ) = destination.writeSizeF( value )
        }

        implicit val `Write[SparceBooleanArray]`: Write[SparseBooleanArray] = Write( _.writeSparseBooleanArray( _ ) )

        implicit val `Write[String]`: Write[String] = Write( _.writeString( _ ) )

        implicit val `Write[URL]`: Write[URL] = Write[URL] { ( destination, value ) ⇒
            destination.writeString( value.toExternalForm )
        }
    }
}