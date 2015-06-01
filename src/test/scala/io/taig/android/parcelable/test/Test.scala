package io.taig.android.parcelable.test

import android.os.Parcel
import io.taig.android.parcelable.Creator
import org.robolectric.annotation.Config
import org.scalatest._

import scala.reflect.runtime.universe

@Config( manifest = "src/main/AndroidManifest.xml" )
class	Test
extends	FlatSpec
with	Matchers
with	RobolectricSuite
{
	val mirror = universe.runtimeMirror( getClass.getClassLoader )

	def validate( entity: android.os.Parcelable ) =
	{
		val parcel = Parcel.obtain()
		entity.writeToParcel( parcel, 0 )
		parcel.setDataPosition( 0 )

		val companion = mirror
			.reflectModule( mirror.classSymbol( entity.getClass ).companion.asModule )
			.instance
			.asInstanceOf[Creator[_]]

		val result = companion.CREATOR.createFromParcel( parcel )

		entity shouldBe result
	}

	"@Parcelable annotation" should "support \"primitives\"" in
	{
		validate( Primitive.default )
	}

	it should "support Options" in
	{
		validate( OptionArgument( Some( "asdf" ) ) )
		validate( OptionArgument( None ) )
		validate( OptionParcelableArgument( Some( Primitive.default ) ) )
		validate( OptionParcelableArgument( None ) )
	}

	it should "support nested Options" in
	{
		validate( NestedOptionArgument( Some( Some( "asdf" ) ) ) )
		validate( NestedOptionArgument( Some( None ) ) )
		validate( NestedOptionArgument( None ) )
	}

	it should "support Arrays" in
	{
		validate( PrimitiveArrayArgument( Array() ) )
		validate( PrimitiveArrayArgument( Array( 1, 2, 3 ) ) )
	}

	it should "support parcelable Arrays" in
	{
		validate( ParcelableArrayArgument( Array( Primitive.default, Primitive.default ) ) )
	}

	it should "support Traversables" in
	{
		validate( PrimitiveTraversableArguments( Seq( 1, 2, 3 ), List( 2.9, 2.7, 2.6 ) ) )
	}

	it should "support Maps" in
	{
		validate( PrimitiveMapArgument( Map( 1 -> "a", 2 -> "b" ) ) )
	}

	it should "support Tuples" in
	{
		validate( PrimitiveTupleArgument( ( 3, "asdf", Primitive.default ) ) )
	}
}