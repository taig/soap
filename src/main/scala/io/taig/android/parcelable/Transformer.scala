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

	implicit val byte = new Transformer[Byte]
	{
		override def read( source: Parcel ) = source.readByte()

		override def write( value: Byte, destination: Parcel, flags: Int ) = destination.writeByte( value )
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

	implicit val iBinder = new Transformer[IBinder]
	{
		override def read( source: Parcel ) = source.readStrongBinder()

		override def write( value: IBinder, destination: Parcel, flags: Int ) = destination.writeStrongBinder( value )
	}

	implicit val int = new Transformer[Int]
	{
		override def read( source: Parcel ) = source.readInt()

		override def write( value: Int, destination: Parcel, flags: Int ) = destination.writeInt( value )
	}

	implicit val long = new Transformer[Long]
	{
		override def read( source: Parcel ) = source.readInt()

		override def write( value: Long, destination: Parcel, flags: Int ) = destination.writeLong( value )
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

	implicit def array[T: Transformer]( implicit tag: ClassTag[T] ) = new Transformer[Array[T]]
	{
		val transformer = implicitly[Transformer[T]]

		override def read( source: Parcel ) =
		{
			( 0 until source.readInt() ).map( _ => transformer.read( source ) ).toArray
		}

		override def write( value: Array[T], destination: Parcel, flags: Int ) =
		{
			destination.writeInt( value.size )
			value.foreach( transformer.write( _, destination, flags ) )
		}
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

	implicit def tuple1[T: Transformer] = new Transformer[Tuple1[T]]
	{
		val transformer = implicitly[Transformer[T]]

		override def read( source: Parcel ) = Tuple1( transformer.read( source ) )

		override def write( value: Tuple1[T], destination: Parcel, flags: Int ) =
		{
			transformer.write( value._1, destination, flags )
		}
	}

	implicit def tuple2[T1: Transformer, T2: Transformer] = new Transformer[( T1, T2 )]
	{
		val transformer = new
		{
			val _1 = implicitly[Transformer[T1]]

			val _2 = implicitly[Transformer[T2]]
		}

		override def read( source: Parcel ) = Tuple2(
			transformer._1.read( source ),
			transformer._2.read( source )
		)

		override def write( value: ( T1, T2 ), destination: Parcel, flags: Int ) =
		{
			transformer._1.write( value._1, destination, flags )
			transformer._2.write( value._2, destination, flags )
		}
	}


	implicit def tuple3[T1: Transformer, T2: Transformer, T3: Transformer] = new Transformer[( T1, T2, T3 )]
	{
		val transformer = new
		{
			val _1 = implicitly[Transformer[T1]]

			val _2 = implicitly[Transformer[T2]]

			val _3 = implicitly[Transformer[T3]]
		}

		override def read( source: Parcel ) = Tuple3(
			transformer._1.read( source ),
			transformer._2.read( source ),
			transformer._3.read( source )
		)

		override def write( value: ( T1, T2, T3 ), destination: Parcel, flags: Int ) =
		{
			transformer._1.write( value._1, destination, flags )
			transformer._2.write( value._2, destination, flags )
			transformer._3.write( value._3, destination, flags )
		}
	}


	implicit def tuple4[T1: Transformer, T2: Transformer, T3: Transformer, T4: Transformer] = new Transformer[( T1, T2, T3, T4 )]
	{
		val transformer = new
		{
			val _1 = implicitly[Transformer[T1]]

			val _2 = implicitly[Transformer[T2]]

			val _3 = implicitly[Transformer[T3]]

			val _4 = implicitly[Transformer[T4]]
		}

		override def read( source: Parcel ) = Tuple4(
			transformer._1.read( source ),
			transformer._2.read( source ),
			transformer._3.read( source ),
			transformer._4.read( source )
		)

		override def write( value: ( T1, T2, T3, T4 ), destination: Parcel, flags: Int ) =
		{
			transformer._1.write( value._1, destination, flags )
			transformer._2.write( value._2, destination, flags )
			transformer._3.write( value._3, destination, flags )
			transformer._4.write( value._4, destination, flags )
		}
	}


	implicit def tuple5[T1: Transformer, T2: Transformer, T3: Transformer, T4: Transformer, T5: Transformer] = new Transformer[( T1, T2, T3, T4, T5 )]
	{
		val transformer = new
		{
			val _1 = implicitly[Transformer[T1]]

			val _2 = implicitly[Transformer[T2]]

			val _3 = implicitly[Transformer[T3]]

			val _4 = implicitly[Transformer[T4]]

			val _5 = implicitly[Transformer[T5]]
		}

		override def read( source: Parcel ) = Tuple5(
			transformer._1.read( source ),
			transformer._2.read( source ),
			transformer._3.read( source ),
			transformer._4.read( source ),
			transformer._5.read( source )
		)

		override def write( value: ( T1, T2, T3, T4, T5 ), destination: Parcel, flags: Int ) =
		{
			transformer._1.write( value._1, destination, flags )
			transformer._2.write( value._2, destination, flags )
			transformer._3.write( value._3, destination, flags )
			transformer._4.write( value._4, destination, flags )
			transformer._5.write( value._5, destination, flags )
		}
	}


	implicit def tuple6[T1: Transformer, T2: Transformer, T3: Transformer, T4: Transformer, T5: Transformer, T6: Transformer] = new Transformer[( T1, T2, T3, T4, T5, T6 )]
	{
		val transformer = new
		{
			val _1 = implicitly[Transformer[T1]]

			val _2 = implicitly[Transformer[T2]]

			val _3 = implicitly[Transformer[T3]]

			val _4 = implicitly[Transformer[T4]]

			val _5 = implicitly[Transformer[T5]]

			val _6 = implicitly[Transformer[T6]]
		}

		override def read( source: Parcel ) = Tuple6(
			transformer._1.read( source ),
			transformer._2.read( source ),
			transformer._3.read( source ),
			transformer._4.read( source ),
			transformer._5.read( source ),
			transformer._6.read( source )
		)

		override def write( value: ( T1, T2, T3, T4, T5, T6 ), destination: Parcel, flags: Int ) =
		{
			transformer._1.write( value._1, destination, flags )
			transformer._2.write( value._2, destination, flags )
			transformer._3.write( value._3, destination, flags )
			transformer._4.write( value._4, destination, flags )
			transformer._5.write( value._5, destination, flags )
			transformer._6.write( value._6, destination, flags )
		}
	}


	implicit def tuple7[T1: Transformer, T2: Transformer, T3: Transformer, T4: Transformer, T5: Transformer, T6: Transformer, T7: Transformer] = new Transformer[( T1, T2, T3, T4, T5, T6, T7 )]
	{
		val transformer = new
		{
			val _1 = implicitly[Transformer[T1]]

			val _2 = implicitly[Transformer[T2]]

			val _3 = implicitly[Transformer[T3]]

			val _4 = implicitly[Transformer[T4]]

			val _5 = implicitly[Transformer[T5]]

			val _6 = implicitly[Transformer[T6]]

			val _7 = implicitly[Transformer[T7]]
		}

		override def read( source: Parcel ) = Tuple7(
			transformer._1.read( source ),
			transformer._2.read( source ),
			transformer._3.read( source ),
			transformer._4.read( source ),
			transformer._5.read( source ),
			transformer._6.read( source ),
			transformer._7.read( source )
		)

		override def write( value: ( T1, T2, T3, T4, T5, T6, T7 ), destination: Parcel, flags: Int ) =
		{
			transformer._1.write( value._1, destination, flags )
			transformer._2.write( value._2, destination, flags )
			transformer._3.write( value._3, destination, flags )
			transformer._4.write( value._4, destination, flags )
			transformer._5.write( value._5, destination, flags )
			transformer._6.write( value._6, destination, flags )
			transformer._7.write( value._7, destination, flags )
		}
	}


	implicit def tuple8[T1: Transformer, T2: Transformer, T3: Transformer, T4: Transformer, T5: Transformer, T6: Transformer, T7: Transformer, T8: Transformer] = new Transformer[( T1, T2, T3, T4, T5, T6, T7, T8 )]
	{
		val transformer = new
		{
			val _1 = implicitly[Transformer[T1]]

			val _2 = implicitly[Transformer[T2]]

			val _3 = implicitly[Transformer[T3]]

			val _4 = implicitly[Transformer[T4]]

			val _5 = implicitly[Transformer[T5]]

			val _6 = implicitly[Transformer[T6]]

			val _7 = implicitly[Transformer[T7]]

			val _8 = implicitly[Transformer[T8]]
		}

		override def read( source: Parcel ) = Tuple8(
			transformer._1.read( source ),
			transformer._2.read( source ),
			transformer._3.read( source ),
			transformer._4.read( source ),
			transformer._5.read( source ),
			transformer._6.read( source ),
			transformer._7.read( source ),
			transformer._8.read( source )
		)

		override def write( value: ( T1, T2, T3, T4, T5, T6, T7, T8 ), destination: Parcel, flags: Int ) =
		{
			transformer._1.write( value._1, destination, flags )
			transformer._2.write( value._2, destination, flags )
			transformer._3.write( value._3, destination, flags )
			transformer._4.write( value._4, destination, flags )
			transformer._5.write( value._5, destination, flags )
			transformer._6.write( value._6, destination, flags )
			transformer._7.write( value._7, destination, flags )
			transformer._8.write( value._8, destination, flags )
		}
	}


	implicit def tuple9[T1: Transformer, T2: Transformer, T3: Transformer, T4: Transformer, T5: Transformer, T6: Transformer, T7: Transformer, T8: Transformer, T9: Transformer] = new Transformer[( T1, T2, T3, T4, T5, T6, T7, T8, T9 )]
	{
		val transformer = new
		{
			val _1 = implicitly[Transformer[T1]]

			val _2 = implicitly[Transformer[T2]]

			val _3 = implicitly[Transformer[T3]]

			val _4 = implicitly[Transformer[T4]]

			val _5 = implicitly[Transformer[T5]]

			val _6 = implicitly[Transformer[T6]]

			val _7 = implicitly[Transformer[T7]]

			val _8 = implicitly[Transformer[T8]]

			val _9 = implicitly[Transformer[T9]]
		}

		override def read( source: Parcel ) = Tuple9(
			transformer._1.read( source ),
			transformer._2.read( source ),
			transformer._3.read( source ),
			transformer._4.read( source ),
			transformer._5.read( source ),
			transformer._6.read( source ),
			transformer._7.read( source ),
			transformer._8.read( source ),
			transformer._9.read( source )
		)

		override def write( value: ( T1, T2, T3, T4, T5, T6, T7, T8, T9 ), destination: Parcel, flags: Int ) =
		{
			transformer._1.write( value._1, destination, flags )
			transformer._2.write( value._2, destination, flags )
			transformer._3.write( value._3, destination, flags )
			transformer._4.write( value._4, destination, flags )
			transformer._5.write( value._5, destination, flags )
			transformer._6.write( value._6, destination, flags )
			transformer._7.write( value._7, destination, flags )
			transformer._8.write( value._8, destination, flags )
			transformer._9.write( value._9, destination, flags )
		}
	}


	implicit def tuple10[T1: Transformer, T2: Transformer, T3: Transformer, T4: Transformer, T5: Transformer, T6: Transformer, T7: Transformer, T8: Transformer, T9: Transformer, T10: Transformer] = new Transformer[( T1, T2, T3, T4, T5, T6, T7, T8, T9, T10 )]
	{
		val transformer = new
		{
			val _1 = implicitly[Transformer[T1]]

			val _2 = implicitly[Transformer[T2]]

			val _3 = implicitly[Transformer[T3]]

			val _4 = implicitly[Transformer[T4]]

			val _5 = implicitly[Transformer[T5]]

			val _6 = implicitly[Transformer[T6]]

			val _7 = implicitly[Transformer[T7]]

			val _8 = implicitly[Transformer[T8]]

			val _9 = implicitly[Transformer[T9]]

			val _10 = implicitly[Transformer[T10]]
		}

		override def read( source: Parcel ) = Tuple10(
			transformer._1.read( source ),
			transformer._2.read( source ),
			transformer._3.read( source ),
			transformer._4.read( source ),
			transformer._5.read( source ),
			transformer._6.read( source ),
			transformer._7.read( source ),
			transformer._8.read( source ),
			transformer._9.read( source ),
			transformer._10.read( source )
		)

		override def write( value: ( T1, T2, T3, T4, T5, T6, T7, T8, T9, T10 ), destination: Parcel, flags: Int ) =
		{
			transformer._1.write( value._1, destination, flags )
			transformer._2.write( value._2, destination, flags )
			transformer._3.write( value._3, destination, flags )
			transformer._4.write( value._4, destination, flags )
			transformer._5.write( value._5, destination, flags )
			transformer._6.write( value._6, destination, flags )
			transformer._7.write( value._7, destination, flags )
			transformer._8.write( value._8, destination, flags )
			transformer._9.write( value._9, destination, flags )
			transformer._10.write( value._10, destination, flags )
		}
	}


	implicit def tuple11[T1: Transformer, T2: Transformer, T3: Transformer, T4: Transformer, T5: Transformer, T6: Transformer, T7: Transformer, T8: Transformer, T9: Transformer, T10: Transformer, T11: Transformer] = new Transformer[( T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11 )]
	{
		val transformer = new
		{
			val _1 = implicitly[Transformer[T1]]

			val _2 = implicitly[Transformer[T2]]

			val _3 = implicitly[Transformer[T3]]

			val _4 = implicitly[Transformer[T4]]

			val _5 = implicitly[Transformer[T5]]

			val _6 = implicitly[Transformer[T6]]

			val _7 = implicitly[Transformer[T7]]

			val _8 = implicitly[Transformer[T8]]

			val _9 = implicitly[Transformer[T9]]

			val _10 = implicitly[Transformer[T10]]

			val _11 = implicitly[Transformer[T11]]
		}

		override def read( source: Parcel ) = Tuple11(
			transformer._1.read( source ),
			transformer._2.read( source ),
			transformer._3.read( source ),
			transformer._4.read( source ),
			transformer._5.read( source ),
			transformer._6.read( source ),
			transformer._7.read( source ),
			transformer._8.read( source ),
			transformer._9.read( source ),
			transformer._10.read( source ),
			transformer._11.read( source )
		)

		override def write( value: ( T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11 ), destination: Parcel, flags: Int ) =
		{
			transformer._1.write( value._1, destination, flags )
			transformer._2.write( value._2, destination, flags )
			transformer._3.write( value._3, destination, flags )
			transformer._4.write( value._4, destination, flags )
			transformer._5.write( value._5, destination, flags )
			transformer._6.write( value._6, destination, flags )
			transformer._7.write( value._7, destination, flags )
			transformer._8.write( value._8, destination, flags )
			transformer._9.write( value._9, destination, flags )
			transformer._10.write( value._10, destination, flags )
			transformer._11.write( value._11, destination, flags )
		}
	}


	implicit def tuple12[T1: Transformer, T2: Transformer, T3: Transformer, T4: Transformer, T5: Transformer, T6: Transformer, T7: Transformer, T8: Transformer, T9: Transformer, T10: Transformer, T11: Transformer, T12: Transformer] = new Transformer[( T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12 )]
	{
		val transformer = new
		{
			val _1 = implicitly[Transformer[T1]]

			val _2 = implicitly[Transformer[T2]]

			val _3 = implicitly[Transformer[T3]]

			val _4 = implicitly[Transformer[T4]]

			val _5 = implicitly[Transformer[T5]]

			val _6 = implicitly[Transformer[T6]]

			val _7 = implicitly[Transformer[T7]]

			val _8 = implicitly[Transformer[T8]]

			val _9 = implicitly[Transformer[T9]]

			val _10 = implicitly[Transformer[T10]]

			val _11 = implicitly[Transformer[T11]]

			val _12 = implicitly[Transformer[T12]]
		}

		override def read( source: Parcel ) = Tuple12(
			transformer._1.read( source ),
			transformer._2.read( source ),
			transformer._3.read( source ),
			transformer._4.read( source ),
			transformer._5.read( source ),
			transformer._6.read( source ),
			transformer._7.read( source ),
			transformer._8.read( source ),
			transformer._9.read( source ),
			transformer._10.read( source ),
			transformer._11.read( source ),
			transformer._12.read( source )
		)

		override def write( value: ( T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12 ), destination: Parcel, flags: Int ) =
		{
			transformer._1.write( value._1, destination, flags )
			transformer._2.write( value._2, destination, flags )
			transformer._3.write( value._3, destination, flags )
			transformer._4.write( value._4, destination, flags )
			transformer._5.write( value._5, destination, flags )
			transformer._6.write( value._6, destination, flags )
			transformer._7.write( value._7, destination, flags )
			transformer._8.write( value._8, destination, flags )
			transformer._9.write( value._9, destination, flags )
			transformer._10.write( value._10, destination, flags )
			transformer._11.write( value._11, destination, flags )
			transformer._12.write( value._12, destination, flags )
		}
	}


	implicit def tuple13[T1: Transformer, T2: Transformer, T3: Transformer, T4: Transformer, T5: Transformer, T6: Transformer, T7: Transformer, T8: Transformer, T9: Transformer, T10: Transformer, T11: Transformer, T12: Transformer, T13: Transformer] = new Transformer[( T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13 )]
	{
		val transformer = new
		{
			val _1 = implicitly[Transformer[T1]]

			val _2 = implicitly[Transformer[T2]]

			val _3 = implicitly[Transformer[T3]]

			val _4 = implicitly[Transformer[T4]]

			val _5 = implicitly[Transformer[T5]]

			val _6 = implicitly[Transformer[T6]]

			val _7 = implicitly[Transformer[T7]]

			val _8 = implicitly[Transformer[T8]]

			val _9 = implicitly[Transformer[T9]]

			val _10 = implicitly[Transformer[T10]]

			val _11 = implicitly[Transformer[T11]]

			val _12 = implicitly[Transformer[T12]]

			val _13 = implicitly[Transformer[T13]]
		}

		override def read( source: Parcel ) = Tuple13(
			transformer._1.read( source ),
			transformer._2.read( source ),
			transformer._3.read( source ),
			transformer._4.read( source ),
			transformer._5.read( source ),
			transformer._6.read( source ),
			transformer._7.read( source ),
			transformer._8.read( source ),
			transformer._9.read( source ),
			transformer._10.read( source ),
			transformer._11.read( source ),
			transformer._12.read( source ),
			transformer._13.read( source )
		)

		override def write( value: ( T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13 ), destination: Parcel, flags: Int ) =
		{
			transformer._1.write( value._1, destination, flags )
			transformer._2.write( value._2, destination, flags )
			transformer._3.write( value._3, destination, flags )
			transformer._4.write( value._4, destination, flags )
			transformer._5.write( value._5, destination, flags )
			transformer._6.write( value._6, destination, flags )
			transformer._7.write( value._7, destination, flags )
			transformer._8.write( value._8, destination, flags )
			transformer._9.write( value._9, destination, flags )
			transformer._10.write( value._10, destination, flags )
			transformer._11.write( value._11, destination, flags )
			transformer._12.write( value._12, destination, flags )
			transformer._13.write( value._13, destination, flags )
		}
	}


	implicit def tuple14[T1: Transformer, T2: Transformer, T3: Transformer, T4: Transformer, T5: Transformer, T6: Transformer, T7: Transformer, T8: Transformer, T9: Transformer, T10: Transformer, T11: Transformer, T12: Transformer, T13: Transformer, T14: Transformer] = new Transformer[( T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14 )]
	{
		val transformer = new
		{
			val _1 = implicitly[Transformer[T1]]

			val _2 = implicitly[Transformer[T2]]

			val _3 = implicitly[Transformer[T3]]

			val _4 = implicitly[Transformer[T4]]

			val _5 = implicitly[Transformer[T5]]

			val _6 = implicitly[Transformer[T6]]

			val _7 = implicitly[Transformer[T7]]

			val _8 = implicitly[Transformer[T8]]

			val _9 = implicitly[Transformer[T9]]

			val _10 = implicitly[Transformer[T10]]

			val _11 = implicitly[Transformer[T11]]

			val _12 = implicitly[Transformer[T12]]

			val _13 = implicitly[Transformer[T13]]

			val _14 = implicitly[Transformer[T14]]
		}

		override def read( source: Parcel ) = Tuple14(
			transformer._1.read( source ),
			transformer._2.read( source ),
			transformer._3.read( source ),
			transformer._4.read( source ),
			transformer._5.read( source ),
			transformer._6.read( source ),
			transformer._7.read( source ),
			transformer._8.read( source ),
			transformer._9.read( source ),
			transformer._10.read( source ),
			transformer._11.read( source ),
			transformer._12.read( source ),
			transformer._13.read( source ),
			transformer._14.read( source )
		)

		override def write( value: ( T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14 ), destination: Parcel, flags: Int ) =
		{
			transformer._1.write( value._1, destination, flags )
			transformer._2.write( value._2, destination, flags )
			transformer._3.write( value._3, destination, flags )
			transformer._4.write( value._4, destination, flags )
			transformer._5.write( value._5, destination, flags )
			transformer._6.write( value._6, destination, flags )
			transformer._7.write( value._7, destination, flags )
			transformer._8.write( value._8, destination, flags )
			transformer._9.write( value._9, destination, flags )
			transformer._10.write( value._10, destination, flags )
			transformer._11.write( value._11, destination, flags )
			transformer._12.write( value._12, destination, flags )
			transformer._13.write( value._13, destination, flags )
			transformer._14.write( value._14, destination, flags )
		}
	}


	implicit def tuple15[T1: Transformer, T2: Transformer, T3: Transformer, T4: Transformer, T5: Transformer, T6: Transformer, T7: Transformer, T8: Transformer, T9: Transformer, T10: Transformer, T11: Transformer, T12: Transformer, T13: Transformer, T14: Transformer, T15: Transformer] = new Transformer[( T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15 )]
	{
		val transformer = new
		{
			val _1 = implicitly[Transformer[T1]]

			val _2 = implicitly[Transformer[T2]]

			val _3 = implicitly[Transformer[T3]]

			val _4 = implicitly[Transformer[T4]]

			val _5 = implicitly[Transformer[T5]]

			val _6 = implicitly[Transformer[T6]]

			val _7 = implicitly[Transformer[T7]]

			val _8 = implicitly[Transformer[T8]]

			val _9 = implicitly[Transformer[T9]]

			val _10 = implicitly[Transformer[T10]]

			val _11 = implicitly[Transformer[T11]]

			val _12 = implicitly[Transformer[T12]]

			val _13 = implicitly[Transformer[T13]]

			val _14 = implicitly[Transformer[T14]]

			val _15 = implicitly[Transformer[T15]]
		}

		override def read( source: Parcel ) = Tuple15(
			transformer._1.read( source ),
			transformer._2.read( source ),
			transformer._3.read( source ),
			transformer._4.read( source ),
			transformer._5.read( source ),
			transformer._6.read( source ),
			transformer._7.read( source ),
			transformer._8.read( source ),
			transformer._9.read( source ),
			transformer._10.read( source ),
			transformer._11.read( source ),
			transformer._12.read( source ),
			transformer._13.read( source ),
			transformer._14.read( source ),
			transformer._15.read( source )
		)

		override def write( value: ( T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15 ), destination: Parcel, flags: Int ) =
		{
			transformer._1.write( value._1, destination, flags )
			transformer._2.write( value._2, destination, flags )
			transformer._3.write( value._3, destination, flags )
			transformer._4.write( value._4, destination, flags )
			transformer._5.write( value._5, destination, flags )
			transformer._6.write( value._6, destination, flags )
			transformer._7.write( value._7, destination, flags )
			transformer._8.write( value._8, destination, flags )
			transformer._9.write( value._9, destination, flags )
			transformer._10.write( value._10, destination, flags )
			transformer._11.write( value._11, destination, flags )
			transformer._12.write( value._12, destination, flags )
			transformer._13.write( value._13, destination, flags )
			transformer._14.write( value._14, destination, flags )
			transformer._15.write( value._15, destination, flags )
		}
	}


	implicit def tuple16[T1: Transformer, T2: Transformer, T3: Transformer, T4: Transformer, T5: Transformer, T6: Transformer, T7: Transformer, T8: Transformer, T9: Transformer, T10: Transformer, T11: Transformer, T12: Transformer, T13: Transformer, T14: Transformer, T15: Transformer, T16: Transformer] = new Transformer[( T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16 )]
	{
		val transformer = new
		{
			val _1 = implicitly[Transformer[T1]]

			val _2 = implicitly[Transformer[T2]]

			val _3 = implicitly[Transformer[T3]]

			val _4 = implicitly[Transformer[T4]]

			val _5 = implicitly[Transformer[T5]]

			val _6 = implicitly[Transformer[T6]]

			val _7 = implicitly[Transformer[T7]]

			val _8 = implicitly[Transformer[T8]]

			val _9 = implicitly[Transformer[T9]]

			val _10 = implicitly[Transformer[T10]]

			val _11 = implicitly[Transformer[T11]]

			val _12 = implicitly[Transformer[T12]]

			val _13 = implicitly[Transformer[T13]]

			val _14 = implicitly[Transformer[T14]]

			val _15 = implicitly[Transformer[T15]]

			val _16 = implicitly[Transformer[T16]]
		}

		override def read( source: Parcel ) = Tuple16(
			transformer._1.read( source ),
			transformer._2.read( source ),
			transformer._3.read( source ),
			transformer._4.read( source ),
			transformer._5.read( source ),
			transformer._6.read( source ),
			transformer._7.read( source ),
			transformer._8.read( source ),
			transformer._9.read( source ),
			transformer._10.read( source ),
			transformer._11.read( source ),
			transformer._12.read( source ),
			transformer._13.read( source ),
			transformer._14.read( source ),
			transformer._15.read( source ),
			transformer._16.read( source )
		)

		override def write( value: ( T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16 ), destination: Parcel, flags: Int ) =
		{
			transformer._1.write( value._1, destination, flags )
			transformer._2.write( value._2, destination, flags )
			transformer._3.write( value._3, destination, flags )
			transformer._4.write( value._4, destination, flags )
			transformer._5.write( value._5, destination, flags )
			transformer._6.write( value._6, destination, flags )
			transformer._7.write( value._7, destination, flags )
			transformer._8.write( value._8, destination, flags )
			transformer._9.write( value._9, destination, flags )
			transformer._10.write( value._10, destination, flags )
			transformer._11.write( value._11, destination, flags )
			transformer._12.write( value._12, destination, flags )
			transformer._13.write( value._13, destination, flags )
			transformer._14.write( value._14, destination, flags )
			transformer._15.write( value._15, destination, flags )
			transformer._16.write( value._16, destination, flags )
		}
	}


	implicit def tuple17[T1: Transformer, T2: Transformer, T3: Transformer, T4: Transformer, T5: Transformer, T6: Transformer, T7: Transformer, T8: Transformer, T9: Transformer, T10: Transformer, T11: Transformer, T12: Transformer, T13: Transformer, T14: Transformer, T15: Transformer, T16: Transformer, T17: Transformer] = new Transformer[( T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17 )]
	{
		val transformer = new
		{
			val _1 = implicitly[Transformer[T1]]

			val _2 = implicitly[Transformer[T2]]

			val _3 = implicitly[Transformer[T3]]

			val _4 = implicitly[Transformer[T4]]

			val _5 = implicitly[Transformer[T5]]

			val _6 = implicitly[Transformer[T6]]

			val _7 = implicitly[Transformer[T7]]

			val _8 = implicitly[Transformer[T8]]

			val _9 = implicitly[Transformer[T9]]

			val _10 = implicitly[Transformer[T10]]

			val _11 = implicitly[Transformer[T11]]

			val _12 = implicitly[Transformer[T12]]

			val _13 = implicitly[Transformer[T13]]

			val _14 = implicitly[Transformer[T14]]

			val _15 = implicitly[Transformer[T15]]

			val _16 = implicitly[Transformer[T16]]

			val _17 = implicitly[Transformer[T17]]
		}

		override def read( source: Parcel ) = Tuple17(
			transformer._1.read( source ),
			transformer._2.read( source ),
			transformer._3.read( source ),
			transformer._4.read( source ),
			transformer._5.read( source ),
			transformer._6.read( source ),
			transformer._7.read( source ),
			transformer._8.read( source ),
			transformer._9.read( source ),
			transformer._10.read( source ),
			transformer._11.read( source ),
			transformer._12.read( source ),
			transformer._13.read( source ),
			transformer._14.read( source ),
			transformer._15.read( source ),
			transformer._16.read( source ),
			transformer._17.read( source )
		)

		override def write( value: ( T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17 ), destination: Parcel, flags: Int ) =
		{
			transformer._1.write( value._1, destination, flags )
			transformer._2.write( value._2, destination, flags )
			transformer._3.write( value._3, destination, flags )
			transformer._4.write( value._4, destination, flags )
			transformer._5.write( value._5, destination, flags )
			transformer._6.write( value._6, destination, flags )
			transformer._7.write( value._7, destination, flags )
			transformer._8.write( value._8, destination, flags )
			transformer._9.write( value._9, destination, flags )
			transformer._10.write( value._10, destination, flags )
			transformer._11.write( value._11, destination, flags )
			transformer._12.write( value._12, destination, flags )
			transformer._13.write( value._13, destination, flags )
			transformer._14.write( value._14, destination, flags )
			transformer._15.write( value._15, destination, flags )
			transformer._16.write( value._16, destination, flags )
			transformer._17.write( value._17, destination, flags )
		}
	}


	implicit def tuple18[T1: Transformer, T2: Transformer, T3: Transformer, T4: Transformer, T5: Transformer, T6: Transformer, T7: Transformer, T8: Transformer, T9: Transformer, T10: Transformer, T11: Transformer, T12: Transformer, T13: Transformer, T14: Transformer, T15: Transformer, T16: Transformer, T17: Transformer, T18: Transformer] = new Transformer[( T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18 )]
	{
		val transformer = new
		{
			val _1 = implicitly[Transformer[T1]]

			val _2 = implicitly[Transformer[T2]]

			val _3 = implicitly[Transformer[T3]]

			val _4 = implicitly[Transformer[T4]]

			val _5 = implicitly[Transformer[T5]]

			val _6 = implicitly[Transformer[T6]]

			val _7 = implicitly[Transformer[T7]]

			val _8 = implicitly[Transformer[T8]]

			val _9 = implicitly[Transformer[T9]]

			val _10 = implicitly[Transformer[T10]]

			val _11 = implicitly[Transformer[T11]]

			val _12 = implicitly[Transformer[T12]]

			val _13 = implicitly[Transformer[T13]]

			val _14 = implicitly[Transformer[T14]]

			val _15 = implicitly[Transformer[T15]]

			val _16 = implicitly[Transformer[T16]]

			val _17 = implicitly[Transformer[T17]]

			val _18 = implicitly[Transformer[T18]]
		}

		override def read( source: Parcel ) = Tuple18(
			transformer._1.read( source ),
			transformer._2.read( source ),
			transformer._3.read( source ),
			transformer._4.read( source ),
			transformer._5.read( source ),
			transformer._6.read( source ),
			transformer._7.read( source ),
			transformer._8.read( source ),
			transformer._9.read( source ),
			transformer._10.read( source ),
			transformer._11.read( source ),
			transformer._12.read( source ),
			transformer._13.read( source ),
			transformer._14.read( source ),
			transformer._15.read( source ),
			transformer._16.read( source ),
			transformer._17.read( source ),
			transformer._18.read( source )
		)

		override def write( value: ( T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18 ), destination: Parcel, flags: Int ) =
		{
			transformer._1.write( value._1, destination, flags )
			transformer._2.write( value._2, destination, flags )
			transformer._3.write( value._3, destination, flags )
			transformer._4.write( value._4, destination, flags )
			transformer._5.write( value._5, destination, flags )
			transformer._6.write( value._6, destination, flags )
			transformer._7.write( value._7, destination, flags )
			transformer._8.write( value._8, destination, flags )
			transformer._9.write( value._9, destination, flags )
			transformer._10.write( value._10, destination, flags )
			transformer._11.write( value._11, destination, flags )
			transformer._12.write( value._12, destination, flags )
			transformer._13.write( value._13, destination, flags )
			transformer._14.write( value._14, destination, flags )
			transformer._15.write( value._15, destination, flags )
			transformer._16.write( value._16, destination, flags )
			transformer._17.write( value._17, destination, flags )
			transformer._18.write( value._18, destination, flags )
		}
	}


	implicit def tuple19[T1: Transformer, T2: Transformer, T3: Transformer, T4: Transformer, T5: Transformer, T6: Transformer, T7: Transformer, T8: Transformer, T9: Transformer, T10: Transformer, T11: Transformer, T12: Transformer, T13: Transformer, T14: Transformer, T15: Transformer, T16: Transformer, T17: Transformer, T18: Transformer, T19: Transformer] = new Transformer[( T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19 )]
	{
		val transformer = new
		{
			val _1 = implicitly[Transformer[T1]]

			val _2 = implicitly[Transformer[T2]]

			val _3 = implicitly[Transformer[T3]]

			val _4 = implicitly[Transformer[T4]]

			val _5 = implicitly[Transformer[T5]]

			val _6 = implicitly[Transformer[T6]]

			val _7 = implicitly[Transformer[T7]]

			val _8 = implicitly[Transformer[T8]]

			val _9 = implicitly[Transformer[T9]]

			val _10 = implicitly[Transformer[T10]]

			val _11 = implicitly[Transformer[T11]]

			val _12 = implicitly[Transformer[T12]]

			val _13 = implicitly[Transformer[T13]]

			val _14 = implicitly[Transformer[T14]]

			val _15 = implicitly[Transformer[T15]]

			val _16 = implicitly[Transformer[T16]]

			val _17 = implicitly[Transformer[T17]]

			val _18 = implicitly[Transformer[T18]]

			val _19 = implicitly[Transformer[T19]]
		}

		override def read( source: Parcel ) = Tuple19(
			transformer._1.read( source ),
			transformer._2.read( source ),
			transformer._3.read( source ),
			transformer._4.read( source ),
			transformer._5.read( source ),
			transformer._6.read( source ),
			transformer._7.read( source ),
			transformer._8.read( source ),
			transformer._9.read( source ),
			transformer._10.read( source ),
			transformer._11.read( source ),
			transformer._12.read( source ),
			transformer._13.read( source ),
			transformer._14.read( source ),
			transformer._15.read( source ),
			transformer._16.read( source ),
			transformer._17.read( source ),
			transformer._18.read( source ),
			transformer._19.read( source )
		)

		override def write( value: ( T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19 ), destination: Parcel, flags: Int ) =
		{
			transformer._1.write( value._1, destination, flags )
			transformer._2.write( value._2, destination, flags )
			transformer._3.write( value._3, destination, flags )
			transformer._4.write( value._4, destination, flags )
			transformer._5.write( value._5, destination, flags )
			transformer._6.write( value._6, destination, flags )
			transformer._7.write( value._7, destination, flags )
			transformer._8.write( value._8, destination, flags )
			transformer._9.write( value._9, destination, flags )
			transformer._10.write( value._10, destination, flags )
			transformer._11.write( value._11, destination, flags )
			transformer._12.write( value._12, destination, flags )
			transformer._13.write( value._13, destination, flags )
			transformer._14.write( value._14, destination, flags )
			transformer._15.write( value._15, destination, flags )
			transformer._16.write( value._16, destination, flags )
			transformer._17.write( value._17, destination, flags )
			transformer._18.write( value._18, destination, flags )
			transformer._19.write( value._19, destination, flags )
		}
	}


	implicit def tuple20[T1: Transformer, T2: Transformer, T3: Transformer, T4: Transformer, T5: Transformer, T6: Transformer, T7: Transformer, T8: Transformer, T9: Transformer, T10: Transformer, T11: Transformer, T12: Transformer, T13: Transformer, T14: Transformer, T15: Transformer, T16: Transformer, T17: Transformer, T18: Transformer, T19: Transformer, T20: Transformer] = new Transformer[( T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20 )]
	{
		val transformer = new
		{
			val _1 = implicitly[Transformer[T1]]

			val _2 = implicitly[Transformer[T2]]

			val _3 = implicitly[Transformer[T3]]

			val _4 = implicitly[Transformer[T4]]

			val _5 = implicitly[Transformer[T5]]

			val _6 = implicitly[Transformer[T6]]

			val _7 = implicitly[Transformer[T7]]

			val _8 = implicitly[Transformer[T8]]

			val _9 = implicitly[Transformer[T9]]

			val _10 = implicitly[Transformer[T10]]

			val _11 = implicitly[Transformer[T11]]

			val _12 = implicitly[Transformer[T12]]

			val _13 = implicitly[Transformer[T13]]

			val _14 = implicitly[Transformer[T14]]

			val _15 = implicitly[Transformer[T15]]

			val _16 = implicitly[Transformer[T16]]

			val _17 = implicitly[Transformer[T17]]

			val _18 = implicitly[Transformer[T18]]

			val _19 = implicitly[Transformer[T19]]

			val _20 = implicitly[Transformer[T20]]
		}

		override def read( source: Parcel ) = Tuple20(
			transformer._1.read( source ),
			transformer._2.read( source ),
			transformer._3.read( source ),
			transformer._4.read( source ),
			transformer._5.read( source ),
			transformer._6.read( source ),
			transformer._7.read( source ),
			transformer._8.read( source ),
			transformer._9.read( source ),
			transformer._10.read( source ),
			transformer._11.read( source ),
			transformer._12.read( source ),
			transformer._13.read( source ),
			transformer._14.read( source ),
			transformer._15.read( source ),
			transformer._16.read( source ),
			transformer._17.read( source ),
			transformer._18.read( source ),
			transformer._19.read( source ),
			transformer._20.read( source )
		)

		override def write( value: ( T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20 ), destination: Parcel, flags: Int ) =
		{
			transformer._1.write( value._1, destination, flags )
			transformer._2.write( value._2, destination, flags )
			transformer._3.write( value._3, destination, flags )
			transformer._4.write( value._4, destination, flags )
			transformer._5.write( value._5, destination, flags )
			transformer._6.write( value._6, destination, flags )
			transformer._7.write( value._7, destination, flags )
			transformer._8.write( value._8, destination, flags )
			transformer._9.write( value._9, destination, flags )
			transformer._10.write( value._10, destination, flags )
			transformer._11.write( value._11, destination, flags )
			transformer._12.write( value._12, destination, flags )
			transformer._13.write( value._13, destination, flags )
			transformer._14.write( value._14, destination, flags )
			transformer._15.write( value._15, destination, flags )
			transformer._16.write( value._16, destination, flags )
			transformer._17.write( value._17, destination, flags )
			transformer._18.write( value._18, destination, flags )
			transformer._19.write( value._19, destination, flags )
			transformer._20.write( value._20, destination, flags )
		}
	}


	implicit def tuple21[T1: Transformer, T2: Transformer, T3: Transformer, T4: Transformer, T5: Transformer, T6: Transformer, T7: Transformer, T8: Transformer, T9: Transformer, T10: Transformer, T11: Transformer, T12: Transformer, T13: Transformer, T14: Transformer, T15: Transformer, T16: Transformer, T17: Transformer, T18: Transformer, T19: Transformer, T20: Transformer, T21: Transformer] = new Transformer[( T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21 )]
	{
		val transformer = new
		{
			val _1 = implicitly[Transformer[T1]]

			val _2 = implicitly[Transformer[T2]]

			val _3 = implicitly[Transformer[T3]]

			val _4 = implicitly[Transformer[T4]]

			val _5 = implicitly[Transformer[T5]]

			val _6 = implicitly[Transformer[T6]]

			val _7 = implicitly[Transformer[T7]]

			val _8 = implicitly[Transformer[T8]]

			val _9 = implicitly[Transformer[T9]]

			val _10 = implicitly[Transformer[T10]]

			val _11 = implicitly[Transformer[T11]]

			val _12 = implicitly[Transformer[T12]]

			val _13 = implicitly[Transformer[T13]]

			val _14 = implicitly[Transformer[T14]]

			val _15 = implicitly[Transformer[T15]]

			val _16 = implicitly[Transformer[T16]]

			val _17 = implicitly[Transformer[T17]]

			val _18 = implicitly[Transformer[T18]]

			val _19 = implicitly[Transformer[T19]]

			val _20 = implicitly[Transformer[T20]]

			val _21 = implicitly[Transformer[T21]]
		}

		override def read( source: Parcel ) = Tuple21(
			transformer._1.read( source ),
			transformer._2.read( source ),
			transformer._3.read( source ),
			transformer._4.read( source ),
			transformer._5.read( source ),
			transformer._6.read( source ),
			transformer._7.read( source ),
			transformer._8.read( source ),
			transformer._9.read( source ),
			transformer._10.read( source ),
			transformer._11.read( source ),
			transformer._12.read( source ),
			transformer._13.read( source ),
			transformer._14.read( source ),
			transformer._15.read( source ),
			transformer._16.read( source ),
			transformer._17.read( source ),
			transformer._18.read( source ),
			transformer._19.read( source ),
			transformer._20.read( source ),
			transformer._21.read( source )
		)

		override def write( value: ( T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21 ), destination: Parcel, flags: Int ) =
		{
			transformer._1.write( value._1, destination, flags )
			transformer._2.write( value._2, destination, flags )
			transformer._3.write( value._3, destination, flags )
			transformer._4.write( value._4, destination, flags )
			transformer._5.write( value._5, destination, flags )
			transformer._6.write( value._6, destination, flags )
			transformer._7.write( value._7, destination, flags )
			transformer._8.write( value._8, destination, flags )
			transformer._9.write( value._9, destination, flags )
			transformer._10.write( value._10, destination, flags )
			transformer._11.write( value._11, destination, flags )
			transformer._12.write( value._12, destination, flags )
			transformer._13.write( value._13, destination, flags )
			transformer._14.write( value._14, destination, flags )
			transformer._15.write( value._15, destination, flags )
			transformer._16.write( value._16, destination, flags )
			transformer._17.write( value._17, destination, flags )
			transformer._18.write( value._18, destination, flags )
			transformer._19.write( value._19, destination, flags )
			transformer._20.write( value._20, destination, flags )
			transformer._21.write( value._21, destination, flags )
		}
	}


	implicit def tuple22[T1: Transformer, T2: Transformer, T3: Transformer, T4: Transformer, T5: Transformer, T6: Transformer, T7: Transformer, T8: Transformer, T9: Transformer, T10: Transformer, T11: Transformer, T12: Transformer, T13: Transformer, T14: Transformer, T15: Transformer, T16: Transformer, T17: Transformer, T18: Transformer, T19: Transformer, T20: Transformer, T21: Transformer, T22: Transformer] = new Transformer[( T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22 )]
	{
		val transformer = new
		{
			val _1 = implicitly[Transformer[T1]]

			val _2 = implicitly[Transformer[T2]]

			val _3 = implicitly[Transformer[T3]]

			val _4 = implicitly[Transformer[T4]]

			val _5 = implicitly[Transformer[T5]]

			val _6 = implicitly[Transformer[T6]]

			val _7 = implicitly[Transformer[T7]]

			val _8 = implicitly[Transformer[T8]]

			val _9 = implicitly[Transformer[T9]]

			val _10 = implicitly[Transformer[T10]]

			val _11 = implicitly[Transformer[T11]]

			val _12 = implicitly[Transformer[T12]]

			val _13 = implicitly[Transformer[T13]]

			val _14 = implicitly[Transformer[T14]]

			val _15 = implicitly[Transformer[T15]]

			val _16 = implicitly[Transformer[T16]]

			val _17 = implicitly[Transformer[T17]]

			val _18 = implicitly[Transformer[T18]]

			val _19 = implicitly[Transformer[T19]]

			val _20 = implicitly[Transformer[T20]]

			val _21 = implicitly[Transformer[T21]]

			val _22 = implicitly[Transformer[T22]]
		}

		override def read( source: Parcel ) = Tuple22(
			transformer._1.read( source ),
			transformer._2.read( source ),
			transformer._3.read( source ),
			transformer._4.read( source ),
			transformer._5.read( source ),
			transformer._6.read( source ),
			transformer._7.read( source ),
			transformer._8.read( source ),
			transformer._9.read( source ),
			transformer._10.read( source ),
			transformer._11.read( source ),
			transformer._12.read( source ),
			transformer._13.read( source ),
			transformer._14.read( source ),
			transformer._15.read( source ),
			transformer._16.read( source ),
			transformer._17.read( source ),
			transformer._18.read( source ),
			transformer._19.read( source ),
			transformer._20.read( source ),
			transformer._21.read( source ),
			transformer._22.read( source )
		)

		override def write( value: ( T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22 ), destination: Parcel, flags: Int ) =
		{
			transformer._1.write( value._1, destination, flags )
			transformer._2.write( value._2, destination, flags )
			transformer._3.write( value._3, destination, flags )
			transformer._4.write( value._4, destination, flags )
			transformer._5.write( value._5, destination, flags )
			transformer._6.write( value._6, destination, flags )
			transformer._7.write( value._7, destination, flags )
			transformer._8.write( value._8, destination, flags )
			transformer._9.write( value._9, destination, flags )
			transformer._10.write( value._10, destination, flags )
			transformer._11.write( value._11, destination, flags )
			transformer._12.write( value._12, destination, flags )
			transformer._13.write( value._13, destination, flags )
			transformer._14.write( value._14, destination, flags )
			transformer._15.write( value._15, destination, flags )
			transformer._16.write( value._16, destination, flags )
			transformer._17.write( value._17, destination, flags )
			transformer._18.write( value._18, destination, flags )
			transformer._19.write( value._19, destination, flags )
			transformer._20.write( value._20, destination, flags )
			transformer._21.write( value._21, destination, flags )
			transformer._22.write( value._22, destination, flags )
		}
	}
}