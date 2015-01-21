package com.taig.android.parcelable.generator.`object`

import com.taig.android.parcelable.generator.Context

import scala.reflect.macros.whitebox

class	Class[C <: whitebox.Context]( val context: C )
extends	Context[C]
{
	import context.universe._

	def apply( moduleDef: context.universe.ModuleDef ) =
	{
		val ModuleDef( _, name, Template( parents, self, _ ) ) = moduleDef

		ClassDef(
			Modifiers( Flag.TRAIT ),
			name.toTypeName,
			List.empty,
			Template(
				parents :+ tq"android.os.Parcelable",
				self,
				List(
					q"override def describeContents(): Int = 0",
					q"override def writeToParcel( destination: android.os.Parcel, flags: Int ) {}"
				)
			)
		)
	}
}

object Class
{
	def apply( context: whitebox.Context ) = new Class[context.type]( context )
}