package io.taig.android.parcelable

import java.io.FileDescriptor

import android.os._
import android.text.TextUtils
import android.util.{Size, SizeF, SparseBooleanArray}

import scala.annotation.implicitNotFound
import scala.collection.breakOut
import scala.collection.generic.CanBuildFrom
import scala.language.{higherKinds, reflectiveCalls}
import scala.reflect._

/**
 * Instructions on how to parcel and unparcel an object of type T
 */
@implicitNotFound( "No implicit Transformer for type ${T} in scope" )
trait Transformer[T]
{
	def read( source: Parcel ): T

	def write( value: T, destination: Parcel, flags: Int ): Unit
}

object Transformer
{
	implicit def option[T: Transformer] = new Transformer[Option[T]]
	{
		val transformer = implicitly[Transformer[T]]

		override def read( source: Parcel ) = source.readInt() match
		{
			case 1 => Some( transformer.read( source ) )
			case -1 => None
		}

		override def write( value: Option[T], destination: Parcel, flags: Int ) = value match
		{
			case Some( value ) =>
			{
				destination.writeInt( 1 )
				transformer.write( value, destination, flags )
			}
			case None => destination.writeInt( -1 )
		}
	}

	implicit def parcelable[T <: Parcelable: ClassTag] = new Transformer[T]
	{
		override def read( source: Parcel ) =
		{
			source.readParcelable[T]( classTag[T].runtimeClass.getClassLoader )
		}

		override def write( value: T, destination: Parcel, flags: Int ) = destination.writeParcelable( value, flags )
	}

	implicit def traversable[L[B] <: Traversable[B], T: Transformer]( implicit cbf: CanBuildFrom[Nothing, T, L[T]] ) = new Transformer[L[T]]
	{
		val transformer = implicitly[Transformer[T]]

		override def read( source: Parcel ) =
		{
			( 0 until source.readInt() ).map( _ => transformer.read( source ) )( breakOut )
		}

		override def write( value: L[T], destination: Parcel, flags: Int ) =
		{
			destination.writeInt( value.size )
			value.foreach( transformer.write( _, destination, flags ) )
		}
	}

	implicit def map[M[A, B] <: Map[A, B], S: Transformer, T: Transformer]( implicit cbf: CanBuildFrom[Nothing, ( S, T ), M[S, T]] ) = new Transformer[M[S, T]]
	{
		val transformer = new
		{
			val key = implicitly[Transformer[S]]

			val value = implicitly[Transformer[T]]
		}

		override def read( source: Parcel ) =
		{
			( 0 until source.readInt() )
				.map( _ => transformer.key.read( source ) )
				.map( ( _, transformer.value.read( source ) ) )( breakOut )
		}

		override def write( value: M[S, T], destination: Parcel, flags: Int ) =
		{
			destination.writeInt( value.size )
			value.keys.foreach( transformer.key.write( _, destination, flags ) )
			value.values.foreach( transformer.value.write( _, destination, flags ) )
		}
	}

	implicit val bundle = new Transformer[Bundle]
	{
		override def read( source: Parcel ) = source.readBundle()

		override def write( value: Bundle, destination: Parcel, flags: Int ) = destination.writeBundle( value )
	}

	implicit val boolean = new Transformer[Boolean]
	{
		override def read( source: Parcel ) = source.readValue( classOf[Boolean].getClassLoader ).asInstanceOf[Boolean]

		override def write( value: Boolean, destination: Parcel, flags: Int ) = destination.writeValue( value )
	}

	implicit val booleanArray = new Transformer[Array[Boolean]]
	{
		override def read( source: Parcel ) = source.createBooleanArray()

		override def write( value: Array[Boolean], destination: Parcel, flags: Int ) =
		{
			destination.writeBooleanArray( value )
		}
	}

	implicit val byte = new Transformer[Byte]
	{
		override def read( source: Parcel ) = source.readByte()

		override def write( value: Byte, destination: Parcel, flags: Int ) = destination.writeByte( value )
	}

	implicit val byteArray = new Transformer[Array[Byte]]
	{
		override def read( source: Parcel ) = source.createByteArray()

		override def write( value: Array[Byte], destination: Parcel, flags: Int ) =
		{
			destination.writeByteArray( value )
		}
	}

	implicit val char = new Transformer[Char]
	{
		override def read( source: Parcel ) = source.readInt().toChar

		override def write( value: Char, destination: Parcel, flags: Int ) = destination.writeInt( value.toInt )
	}

	implicit val charSequence = new Transformer[CharSequence]
	{
		override def read( source: Parcel ) = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel( source )

		override def write( value: CharSequence, destination: Parcel, flags: Int ) =
		{
			TextUtils.writeToParcel( value, destination, flags )
		}
	}

	implicit val double = new Transformer[Double]
	{
		override def read( source: Parcel ) = source.readDouble()

		override def write( value: Double, destination: Parcel, flags: Int ) = destination.writeDouble( value )
	}

	implicit val doubleArray = new Transformer[Array[Double]]
	{
		override def read( source: Parcel ) = source.createDoubleArray()

		override def write( value: Array[Double], destination: Parcel, flags: Int ) =
		{
			destination.writeDoubleArray( value )
		}
	}

	implicit val fileDescriptor = new Transformer[FileDescriptor]
	{
		override def read( source: Parcel ) = source.readFileDescriptor().getFileDescriptor

		override def write( value: FileDescriptor, destination: Parcel, flags: Int ) = destination.writeFileDescriptor( value )
	}

	implicit val float = new Transformer[Float]
	{
		override def read( source: Parcel ) = source.readFloat()

		override def write( value: Float, destination: Parcel, flags: Int ) = destination.writeFloat( value )
	}

	implicit val floatArray = new Transformer[Array[Float]]
	{
		override def read( source: Parcel ) = source.createFloatArray()

		override def write( value: Array[Float], destination: Parcel, flags: Int ) =
		{
			destination.writeFloatArray( value )
		}
	}

	implicit val iBinder = new Transformer[IBinder]
	{
		override def read( source: Parcel ) = source.readStrongBinder()

		override def write( value: IBinder, destination: Parcel, flags: Int ) = destination.writeStrongBinder( value )
	}

	implicit val iBinderArray = new Transformer[Array[IBinder]]
	{
		override def read( source: Parcel ) = source.createBinderArray()

		override def write( value: Array[IBinder], destination: Parcel, flags: Int ) =
		{
			destination.writeBinderArray( value )
		}
	}

	implicit val int = new Transformer[Int]
	{
		override def read( source: Parcel ) = source.readInt()

		override def write( value: Int, destination: Parcel, flags: Int ) = destination.writeInt( value )
	}

	implicit val intArray = new Transformer[Array[Int]]
	{
		override def read( source: Parcel ) = source.createIntArray()

		override def write( value: Array[Int], destination: Parcel, flags: Int ) =
		{
			destination.writeIntArray( value )
		}
	}

	implicit val long = new Transformer[Long]
	{
		override def read( source: Parcel ) = source.readInt()

		override def write( value: Long, destination: Parcel, flags: Int ) = destination.writeLong( value )
	}

	implicit val longArray = new Transformer[Array[Long]]
	{
		override def read( source: Parcel ) = source.createLongArray()

		override def write( value: Array[Long], destination: Parcel, flags: Int ) = destination.writeLongArray( value )
	}

	implicit val persistableBundle = new Transformer[PersistableBundle]
	{
		override def read( source: Parcel ) = source.readPersistableBundle()

		override def write( value: PersistableBundle, destination: Parcel, flags: Int ) =
		{
			destination.writePersistableBundle( value )
		}
	}

	implicit val short = new Transformer[Short]
	{
		override def read( source: Parcel ) = source.readValue( classOf[Short].getClassLoader ).asInstanceOf[Short]

		override def write( value: Short, destination: Parcel, flags: Int ) = destination.writeValue( value )
	}

	implicit val size = new Transformer[Size]
	{
		override def read( source: Parcel ) = source.readSize()

		override def write( value: Size, destination: Parcel, flags: Int ) = destination.writeSize( value )
	}

	implicit val sizeF = new Transformer[SizeF]
	{
		override def read( source: Parcel ) = source.readSizeF()

		override def write( value: SizeF, destination: Parcel, flags: Int ) = destination.writeSizeF( value )
	}

	implicit val sparseBooleanArray = new Transformer[SparseBooleanArray]
	{
		override def read( source: Parcel ) = source.readSparseBooleanArray()

		override def write( value: SparseBooleanArray, destination: Parcel, flags: Int ) =
		{
			destination.writeSparseBooleanArray( value )
		}
	}

	implicit val string = new Transformer[String]
	{
		override def read( source: Parcel ) = source.readString()

		override def write( value: String, destination: Parcel, flags: Int ) = destination.writeString( value )
	}

	implicit val stringArray = new Transformer[Array[String]]
	{
		override def read( source: Parcel ) = source.createStringArray()

		override def write( value: Array[String], destination: Parcel, flags: Int ) =
		{
			destination.writeStringArray( value )
		}
	}
}