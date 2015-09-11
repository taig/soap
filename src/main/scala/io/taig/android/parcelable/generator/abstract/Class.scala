package io.taig.android.parcelable.generator.`abstract`

import io.taig.android.parcelable.generator.Context

import scala.reflect.macros.whitebox

class Class[C <: whitebox.Context]( val context: C )
        extends Context[C] {
    import context.universe._

    def apply( classDef: context.universe.ClassDef ) =
        {
            val ClassDef( modifiers, name, types, Template( parents, self, body ) ) = classDef

            if ( classDef.extendsFrom[android.os.Parcelable] ) {
                classDef
            } else {
                ClassDef(
                    modifiers,
                    name,
                    types,
                    Template( parents :+ tq"android.os.Parcelable", self, body )
                )
            }
        }
}

object Class {
    def apply( context: whitebox.Context ) = new Class[context.type]( context )
}