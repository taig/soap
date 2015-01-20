package com.taig.android.parcelable.annotation

import com.taig.android.parcelable.`macro`._

import scala.annotation.StaticAnnotation

class	Parcelable
extends	StaticAnnotation
{
	def macroTransform( annottees: Any* ): Any = macro parcelable
}