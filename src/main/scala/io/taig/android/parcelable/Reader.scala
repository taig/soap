package io.taig.android.parcelable

import android.os.{Bundle, Parcel}

import scala.language.implicitConversions

trait	Reader[T]
extends	( Parcel => T )
{
	override def apply( source: Parcel ): T
}

object Reader
{
	implicit def `Parcel => T -> Reader[T]`[T]( reader: ( Parcel ) => T ): Reader[T] =
	{
		new Reader[T]
		{
			override def apply( source: Parcel ) = reader( source )
		}
	}

	implicit val bundle: Reader[Bundle] = ( source: Parcel ) => source.readBundle()

	implicit val int: Reader[Int] = ( source: Parcel ) => source.readInt()

	implicit val string: Reader[String] = ( source: Parcel ) => source.readString()
}