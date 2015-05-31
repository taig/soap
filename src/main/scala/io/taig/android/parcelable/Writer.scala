package io.taig.android.parcelable

import android.os.Parcel

trait	Writer[T]
extends	( ( T, Parcel ) => Unit )
{
	override def apply( value: T, destintation: Parcel ): Unit
}

object Writer
{
	implicit object	String
	extends			Writer[String]
	{
		override def apply( value: String, destintation: Parcel ) = destintation.writeString( value )
	}
}