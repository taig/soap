package io.taig.android.parcelable.annotation

import io.taig.android.parcelable.Macro

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros

@deprecated( "Use io.taig.android.Parcelable", "1.2.6" )
class	Parcelable
extends	StaticAnnotation
{
	 def macroTransform( annottees: Any* ): Any = macro Macro.parcelable
}