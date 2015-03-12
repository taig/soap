package io.taig.android.parcelable

import android.graphics.Rect
import android.os.Parcel
import io.taig.android.Parcelable

import scala.collection.mutable.ArrayBuffer

package object test
{
	@Parcelable
	abstract class AbstractClassWithCompanion

	object	AbstractClassWithCompanion
	extends	Serializable

	@Parcelable
	abstract class AbstractClassWithoutCompanion
	{
		def describeContents() = 0

		def writeToParcel( destination: Parcel, flags: Int ) = {}
	}

	@Parcelable
	class	ClassExtendsParcelable
	extends	android.os.Parcelable
	{
		override def describeContents() = 0

		override def writeToParcel( destination: Parcel, flags: Int ) = {}
	}

	@Parcelable
	case class ClassWithArguments( x: Int = 10, y: String = "asdf", var z: Float = 3.5f )

	@Parcelable
	class ClassWithCompanion

	object	ClassWithCompanion
	extends	Serializable

	@Parcelable
	class ClassWithCreatorCompanion

	object	ClassWithCreatorCompanion
	extends	Creator[ClassWithCreatorCompanion]
	{
		override val CREATOR: android.os.Parcelable.Creator[ClassWithCreatorCompanion] = null
	}

	@Parcelable
	case class ClassWithBooleanArgument( x: Boolean = true, y: Boolean = false )

	@Parcelable
	case class ClassWithMapArgument(
		x: Map[Int, Boolean] = Map( 1 -> true, 2 -> false, 5 -> false ),
		y: Map[String, Rect] = Map( "left" -> new Rect( 5, 6, 7, 8 ), "right" -> new Rect( 1, 2, 3, 4 ) )
	)

	@Parcelable
	case class ClassWithOptionArray(
		x: Option[Array[String]] = Some( Array( "a", "s", "d", "f" ) ),
		y: Option[Array[String]] = None
	)
	{
		override def equals( o: scala.Any ) = o match 
		{
			case c: ClassWithOptionArray => x.map( _.deep ) == c.x.map( _.deep ) && y.map( _.deep ) == c.y.map( _.deep )
			case _ => false
		}
	}
	@Parcelable
	case class ClassWithOptionMap(
		x: Option[Map[Int, String]] = Some( Map( 1 -> "a", 2 -> "s", 3 -> "d", 4 -> "f" ) ),
		y: Option[Map[Int, String]] = None
	)
	@Parcelable
	case class ClassWithOptionTraversable(
		x: Option[Seq[String]] = Some( Seq( "a", "s", "d", "f" ) ),
		y: Option[Seq[String]] = None
	)
	@Parcelable
	case class ClassWithOptionTuple(
		x: Option[(Int, String, Float)] = Some( 1, "asdf", 3.7f ),
		y: Option[(Int, Float)] = None
	)

	@Parcelable
	case class ClassWithOptionArgument( x: Option[Int] = Some( 10 ), y: Option[String] = None )

	@Parcelable
	case class ClassWithShortArgument( x: Short = 0, y: Short = 100 )

	@Parcelable
	case class ClassWithTraversableArgument(
		x: List[Int] = List( 1, 2, 3 ),
		y: Seq[Rect] = Seq( new Rect( 1, 2, 3, 4 ), new Rect( 5, 6, 7, 8 ) ),
		z: ArrayBuffer[String] = ArrayBuffer( "a", "s", "d", "f!" )
	)

	@Parcelable
	case class ClassWithTupleArgument( x: ( String, Int, Short ) = ( "a", 10, 1 ), y: Tuple1[Long] = Tuple1( 1L ) )

	@Parcelable
	class ClassWithoutCompanion

	@Parcelable
	trait	TraitExtendsParcelable
	extends	android.os.Parcelable
	{
		override def describeContents() = 0

		override def writeToParcel( dest: Parcel, flags: Int ) = {}
	}

	@Parcelable
	trait TraitWithCreatorCompanion
	{
		def describeContents() = 0

		def writeToParcel( destination: Parcel, flags: Int ) = {}
	}

	object	TraitWithCreatorCompanion
	extends	Creator[TraitWithCreatorCompanion]
	{
		override val CREATOR: android.os.Parcelable.Creator[TraitWithCreatorCompanion] = null
	}

	@Parcelable
	trait TraitWithoutCompanion
	{
		def describeContents() = 0

		def writeToParcel( destination: Parcel, flags: Int ) = {}
	}

	@Parcelable
	trait Value
	@Parcelable
	case class Absolute( value: Int ) extends Value
	@Parcelable
	case class Relative( value: Float ) extends Value
	@Parcelable
	object Auto extends Value

	@Parcelable
	abstract class Model[M <: Model[M]]
	@Parcelable
	case class Address( street: String = "Unter den Linden 1", city: String = "Berlin" ) extends Model[Address]

	@Parcelable
	case class UseTheSerializableIndirectly( x: ShouldTriggerWarning )

	@Parcelable
	case class UseTheSerializableDirectly( x: ShouldNotTriggerWarning )

	@Parcelable
	class ClassWithParameterGroups( x: Int )( y: String )
}