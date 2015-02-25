package io.taig.android.parcelable.annotation

import io.taig.android.parcelable.`macro`.parcelable

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros

class	Parcelable
extends	StaticAnnotation
{
	def macroTransform( annottees: Any* ): Any = macro parcelable
}