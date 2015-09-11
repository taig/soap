package io.taig.android.parcelable.generator.`class`

import io.taig.android.parcelable.Creator
import io.taig.android.parcelable.generator.Context

import scala.reflect.macros.whitebox

class Companion[C <: whitebox.Context]( val context: C )
        extends Context[C] {
    import context.universe._

    def apply( classDef: ClassDef, moduleDef: ModuleDef ) =
        {
            val ModuleDef( modifiers, name, Template( parents, self, body ) ) = moduleDef

            if ( moduleDef.extendsFrom[Creator[_]] ) {
                moduleDef
            } else {
                ModuleDef(
                    modifiers,
                    name,
                    Template(
                        parents :+ tq"io.taig.android.parcelable.Creator[${name.toTypeName}]",
                        self,
                        body :+ q"""
                        override lazy val CREATOR = new android.os.Parcelable.Creator[${name.toTypeName}] {
                            override def createFromParcel( source: android.os.Parcel ) = ${instantiate( classDef )}

                            override def newArray( size: Int ) = new Array[${name.toTypeName}]( size )
                        }"""
                    )
                )
            }
        }

    private def instantiate( classDef: ClassDef ) =
        {
            def construct( reads: List[List[Tree]] ): Apply = reads match {
                case List( read )  ⇒ Apply( Select( New( Ident( classDef.name ) ), termNames.CONSTRUCTOR ), read )
                case read :: reads ⇒ Apply( construct( reads ), read )
                case Nil           ⇒ construct( List( List.empty ) )
            }

            val reads = classDef
                .getPrimaryConstructor()
                .vparamss
                .map( _.map( _.tpt.resolveType() ) )
                .map( _.map( tpe ⇒ q"implicitly[io.taig.android.parcelable.Transformer[$tpe]].read( source )" ) )

            construct( reads.reverse )
        }
}

object Companion {
    def apply( context: whitebox.Context ) = new Companion[context.type]( context )
}