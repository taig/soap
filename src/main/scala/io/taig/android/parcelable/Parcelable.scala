package io.taig.android.parcelable

import io.taig.android.parcelable.generator.Macro

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros

class Parcelable extends StaticAnnotation {
    def macroTransform( annottees: Any* ): Any = macro Macro.parcelable
}