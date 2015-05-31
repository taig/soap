package io.taig.android.parcelable

import android.os.Parcel

/**
 * Instructions on how to parcel and unparcel an object of type T
 */
trait Transformer[T]
{
	def read( source: Parcel ): T

	def write( value: T, destination: Parcel ): Unit
}

object Transformer
{
	implicit val string = new Transformer[String]
	{
		override def read( source: Parcel ) = source.readString()

		override def write( value: String, destination: Parcel ) = destination.writeString( value )
	}
}