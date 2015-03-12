package io.taig.android.parcelable

import io.taig.android.Parcelable

@Parcelable
class	ClassWithArgumentsInherited(val a: Double = 10.10, x: Int = 100, y: String = "ghjk", z: Float = 0.33f )
extends	ClassWithArguments( x, y, z )
{
	override def equals( o: scala.Any ) = o match
	{
		case c: ClassWithArgumentsInherited => super.equals( o ) && c.a == a
		case _ => false
	}

	override def toString = s"$x, $y, $z, $a"
}