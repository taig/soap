package com.taig.android.parcelable.generator.`object`

import com.taig.android.parcelable.generator.Context

import scala.reflect.macros.whitebox

class	Companion[C <: whitebox.Context]( val context: C )
extends	Context[C]
{
	import context.universe._

	def apply( moduleDef: ModuleDef ) =
	{
		val ModuleDef( modifiers, name, Template( parents, self, body ) ) = moduleDef

		ModuleDef(
			modifiers,
			name,
			Template(
				List( tq"${name.toTypeName}", tq"com.taig.android.parcelable.Creator[${name.toTypeName}]" ),
				self,
				body :+
				q"""
				override lazy val CREATOR = new android.os.Parcelable.Creator[${name.toTypeName}]
				{
					override def createFromParcel( source: android.os.Parcel ) = $name

					override def newArray( size: Int ) = new Array[${name.toTypeName}]( size )
				}
				"""
			)
		)
	}
}

object Companion
{
	def apply( context: whitebox.Context ) = new Companion[context.type]( context )
}