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
                    body :+ q"""
                    override def describeContents(): Int = 0""" :+ q"""
                    override def writeToParcel( destination: android.os.Parcel, flags: Int ): Unit = {
                        import shapeless._
                        import io.taig.android.parcelable._

                        object write extends shapeless.Poly1 {
                            implicit def default[T: Transformer] = at[T]( value => {
                                implicitly[Transformer[T]].write( value, destination, flags )
                            } )
                        }

                        Generic[${name.toTypeName}].to( this ).map( write )
                    }"""
                )
            )
        }
    }
}

object Class {
    def apply( context: whitebox.Context ) = new Class[context.type]( context )
}