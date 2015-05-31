package io.taig.android.parcelable

import android.os.{Bundle, Parcel}

import scala.language.implicitConversions

trait	Writer[T]
extends	( ( T, Parcel ) => Unit )
{
	override def apply( value: T, destintation: Parcel ): Unit
}

object Writer
{
	implicit def `( T, Parcel ) => Unit -> Writer[T]`[T]( writer: ( T, Parcel ) => Unit ): Writer[T] =
	{
		new Writer[T]
		{
			override def apply( value: T, destintation: Parcel ) = writer( value, destintation )
		}
	}

	implicit val bundle: Writer[Bundle] = ( value: Bundle, destination: Parcel ) => destination.writeBundle( value )

	implicit val int: Writer[Int] = ( value: Int, destination: Parcel ) => destination.writeInt( value )

	implicit val string: Writer[String] = ( value: String, destination: Parcel ) => destination.writeString( value )
}