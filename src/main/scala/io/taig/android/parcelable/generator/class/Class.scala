package io.taig.android.parcelable.generator.`class`

import io.taig.android.parcelable.generator.Context

import scala.reflect.macros.whitebox

class Class[C <: whitebox.Context]( val context: C ) extends Context[C] {
    import context.universe._

    def apply( classDef: context.universe.ClassDef ) = {
        val ClassDef( modifiers, name, types, Template( parents, self, body ) ) = classDef

        if ( classDef.hasParent[android.os.Parcelable] ) {
            classDef
        } else {
            ClassDef(
                modifiers,
                name,
                types,
                Template(
                    parents :+ tq"android.os.Parcelable",
                    self,
                    body :+ q"override def describeContents(): Int = 0" :+ q"""
                    override def writeToParcel( destination: android.os.Parcel, flags: Int ): Unit = {
                        ..${classDef.getConstructorFields().map{ case ValDef( _, name, tpe, _ ) ⇒ write( tpe.resolveType(), q"$name" ) }}
                    }"""
                )
            )
        }
    }

    private def write( tpe: Type, name: Tree ): Tree = {
        q"implicitly[io.taig.android.parcelable.Transformer[$tpe]].write( $name, destination, flags )"
    }
}

object Class {
    def apply( context: whitebox.Context ) = new Class[context.type]( context )
}