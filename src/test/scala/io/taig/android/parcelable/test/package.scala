package io.taig.android.parcelable

import io.taig.android.Parcelable

package object test
{
	@Parcelable
	case class OptionArgument( x: Option[String] )

	@Parcelable
	case class OptionParcelableArgument( x: Option[Primitive] )

	@Parcelable
	case class NestedOptionArgument( x: Option[Option[String]] )

	@Parcelable
	case class PrimitiveArrayArgument( x: Array[Int] )
	{
		override def equals( o: scala.Any ) = o match
		{
			case p: PrimitiveArrayArgument => x.sameElements( p.x )
			case _ => false
		}
	}

	@Parcelable
	case class ParcelableArrayArgument( x: Array[Primitive] )
	{
		override def equals( o: scala.Any ) = o match
		{
			case p: ParcelableArrayArgument => x.sameElements( p.x )
			case _ => false
		}
	}

	@Parcelable
	case class PrimitiveTraversableArguments( x: Seq[Int], y: List[Double] )

	@Parcelable
	case class ParcelableTraversableArgument( x: Seq[Primitive] )

	@Parcelable
	case class PrimitiveMapArgument( x: Map[Int, String] )

	@Parcelable
	case class ParcelableMapArgument( x: Map[Int, Primitive] )

	@Parcelable
	case class PrimitiveTupleArgument( x: ( Int, String, Primitive ) )
}