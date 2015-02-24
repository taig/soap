package io.taig.android.parcelable.annotation

import io.taig.android.parcelable.`macro`.parcelable

import scala.annotation.StaticAnnotation

class	Parcelable
extends	StaticAnnotation
{
	def macroTransform( annottees: Any* ): Any = macro parcelable
}