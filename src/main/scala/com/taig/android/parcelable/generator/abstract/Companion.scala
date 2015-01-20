package com.taig.android.parcelable.generator.`abstract`

import com.taig.android.parcelable.Creator
import com.taig.android.parcelable.generator.Context

import scala.reflect.macros.whitebox

class	Companion[C <: whitebox.Context]( val context: C )
extends	Context[C]
{
	import context.universe._

	def apply( moduleDef: ModuleDef ) =
	{
		val ModuleDef( modifiers, name, Template( parents, self, body ) ) = moduleDef

		if( moduleDef.extendsFrom[Creator[_]] )
		{
			moduleDef
		}
		else
		{
			ModuleDef(
				modifiers,
				name,
				Template(
					parents :+ tq"com.taig.android.parcelable.Creator[${name.toTypeName}]",
					self,
					body :+
					q"""
		 			def CREATOR: android.os.Parcelable.Creator[${name.toTypeName}] = sys.error(
						"Can not create an abstract type from parcel. Did you forget to annotate a child class?"
					)"""
				)
			)
		}

	}
}

object Companion
{
	def apply( context: whitebox.Context ) = new Companion[context.type]( context )
}