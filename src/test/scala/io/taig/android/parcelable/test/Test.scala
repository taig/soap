package io.taig.android.parcelable.test

import android.os.Parcel
import io.taig.android.parcelable.Creator
import org.robolectric.annotation.Config
import org.scalatest._

@Config( manifest = "src/main/AndroidManifest.xml" )
class	Test
extends	FlatSpec
with	Matchers
with	RobolectricSuite
{
	"@Parcelable annotation at class / case class" should "generate a companion object, if none is present" in
	{
		ClassWithoutCompanion shouldBe a [Creator[_]]
	}

	it should "use the existing companion object, if present, but keep all definitions" in
	{
		ClassWithCompanion shouldBe a [Serializable]
		ClassWithCompanion shouldBe a [Creator[_]]
	}

	it should "generate a CREATOR field in the companion object" in
	{
		Seq( ClassWithCompanion, ClassWithoutCompanion )
			.map( _.getClass.getMethods.exists( _.getName == "CREATOR" ) )
			.foreach( _ shouldBe true )
	}

	it should "make the class inherit from android.os.Parcelable" in
	{
		Seq( new ClassWithCompanion, new ClassWithoutCompanion )
			.foreach( _ shouldBe a [android.os.Parcelable] )
	}

	it should "leave classes that already implement android.os.Parcelable alone, and only take care of the CREATOR" in
	{
		new ClassExtendsParcelable shouldBe a [android.os.Parcelable]
		ClassExtendsParcelable shouldBe a [Creator[_]]
		ClassExtendsParcelable.getClass.getMethods.exists( _.getName == "CREATOR" ) shouldBe true
	}

	it should "leave companions that already implement Creator alone, and only take care of the class" in
	{
		ClassWithCreatorCompanion shouldBe a [Creator[_]]
		new ClassWithCreatorCompanion shouldBe a [android.os.Parcelable]
	}

	it should "generate working writeToParcel and createFormParcel methods" in
	{
		val parcel = Parcel.obtain()
		val entity = new ClassWithArguments()
		entity.writeToParcel( parcel, 0 )
		parcel.setDataPosition( 0 )

		entity shouldBe ClassWithArguments.CREATOR.createFromParcel( parcel )
	}

	it should "be able to handle Boolean fields" in
	{
		val parcel = Parcel.obtain()
		val entity = new ClassWithBooleanArgument()
		entity.writeToParcel( parcel, 0 )
		parcel.setDataPosition( 0 )

		entity shouldBe ClassWithBooleanArgument.CREATOR.createFromParcel( parcel )
	}

	it should "be able to handle Short fields" in
	{
		val parcel = Parcel.obtain()
		val entity = new ClassWithShortArgument()
		entity.writeToParcel( parcel, 0 )
		parcel.setDataPosition( 0 )

		entity shouldBe ClassWithShortArgument.CREATOR.createFromParcel( parcel )
	}

	it should "be able to handle Option fields" in
	{
		val parcel = Parcel.obtain()
		val entity = new ClassWithOptionArgument()
		entity.writeToParcel( parcel, 0 )
		parcel.setDataPosition( 0 )

		entity shouldBe ClassWithOptionArgument.CREATOR.createFromParcel( parcel )
	}

	it should "be able to handle Tuples (with valid generic parameters) fields" in
	{
		val parcel = Parcel.obtain()
		val entity = new ClassWithTupleArgument()
		entity.writeToParcel( parcel, 0 )
		parcel.setDataPosition( 0 )

		entity shouldBe ClassWithTupleArgument.CREATOR.createFromParcel( parcel )
	}

	it should "be able to handle Traversables (with a valid generic parameter) fields" in
	{
		val parcel = Parcel.obtain()
		val entity = new ClassWithTraversableArgument()
		entity.writeToParcel( parcel, 0 )
		parcel.setDataPosition( 0 )

		entity shouldBe ClassWithTraversableArgument.CREATOR.createFromParcel( parcel )
	}

	it should "be able to handle Map (with a valid generic parameter) fields" in
	{
		val parcel = Parcel.obtain()
		val entity = new ClassWithMapArgument()
		entity.writeToParcel( parcel, 0 )
		parcel.setDataPosition( 0 )

		entity shouldBe ClassWithMapArgument.CREATOR.createFromParcel( parcel )
	}

	it should "support class to class inheritance" in
	{
		val parcel = Parcel.obtain()
		val entity = new ClassWithArgumentsInherited()
		entity.writeToParcel( parcel, 0 )
		parcel.setDataPosition( 0 )

		entity shouldBe ClassWithArgumentsInherited.CREATOR.createFromParcel( parcel )
	}

	it should "be able to handle collections within Option" in
	{
		val entities = Seq(
			( new ClassWithOptionArray(), ClassWithOptionArray.CREATOR ),
			( new ClassWithOptionMap(), ClassWithOptionMap.CREATOR ),
			( new ClassWithOptionTraversable(), ClassWithOptionTraversable.CREATOR ),
			( new ClassWithOptionTuple(), ClassWithOptionTuple.CREATOR )
		)

		entities.foreach
		{
			case ( entity, creator ) =>
			{
				val parcel = Parcel.obtain()
				entity.writeToParcel( parcel, 0 )
				parcel.setDataPosition( 0 )

				entity shouldBe creator.createFromParcel( parcel )
			}
		}
	}

	"@Parcelable annotation at abstract class / trait" should "generate a companion object, if none is present" in
	{
		AbstractClassWithoutCompanion shouldBe a [Creator[_]]
		TraitWithoutCompanion shouldBe a [Creator[_]]
	}

	it should "use the existing companion object, if present, but keep all definitions" in
	{
		AbstractClassWithCompanion shouldBe a [Serializable]
		AbstractClassWithCompanion shouldBe a [Creator[_]]
	}

	it should "fail at providing a valid CREATOR" in
	{
		intercept[RuntimeException]
		{
			TraitWithoutCompanion.CREATOR
		}
	}

	it should "make the class inherit from android.os.Parcelable" in
	{
		new TraitWithoutCompanion {} shouldBe a [android.os.Parcelable]
	}

	it should "leave classes that already implement android.os.Parcelable alone, and only take care of the CREATOR" in
	{
		new TraitExtendsParcelable {} shouldBe a [android.os.Parcelable]
		TraitExtendsParcelable shouldBe a [Creator[_]]
		TraitExtendsParcelable.getClass.getMethods.exists( _.getName == "CREATOR" ) shouldBe true
	}

	it should "leave companions that already implement Creator alone, and only take care of the class" in
	{
		TraitWithCreatorCompanion shouldBe a [Creator[_]]
		new TraitWithCreatorCompanion {} shouldBe a [android.os.Parcelable]
	}

	it should "throw a runtime exception when trying to use an abstract type's CREATOR" in
	{
		intercept[RuntimeException]
		{
			TraitWithoutCompanion.CREATOR
		}
	}

	it should "work with inheritance" in
	{
		val parcel = Parcel.obtain()
		val absolute = Absolute( 10 )
		val relative = Relative( 1f )
		absolute.writeToParcel( parcel, 0 )
		relative.writeToParcel( parcel, 0 )
		parcel.setDataPosition( 0 )

		Absolute.CREATOR.createFromParcel( parcel ) shouldBe absolute
		Relative.CREATOR.createFromParcel( parcel ) shouldBe relative
	}
}