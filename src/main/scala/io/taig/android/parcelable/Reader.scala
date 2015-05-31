package io.taig.android.parcelable

import android.os.Parcel

trait	Reader[T]
extends	( Parcel => T )
{
	override def apply( source: Parcel ): T
}

object Reader
{
	implicit object	String
	extends			Reader[String]
	{
		override def apply( source: Parcel ) = source.readString()
	}
}