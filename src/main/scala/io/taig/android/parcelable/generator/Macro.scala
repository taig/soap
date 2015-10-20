package io.taig.android.parcelable.generator

import scala.reflect.macros.whitebox

object Macro {
    def parcelable( context: whitebox.Context )( annottees: context.Expr[Any]* ): context.Expr[Any] = {
        import context.universe.Flag._
        import context.universe._

        def create( c: ClassDef, m: ModuleDef ): Expr[Any] = ( c, m ) match {
            // Object
            case ( null, m: ModuleDef ) ⇒
                context.Expr( q"""
                    ${`object`.Class( context )( m )}
                    ${`object`.Companion( context )( m )}
                """ )
            // Abstract Class, Trait
            case ( c @ ClassDef( mods, _, _, _ ), m: ModuleDef ) if mods.hasFlag( ABSTRACT ) ⇒
                context.Expr( q"""
                    ${`abstract`.Class( context )( c )}
                    ${`abstract`.Companion( context )( c, m )}
                """ )
            // Case Class
            case ( c @ ClassDef( mods, _, _, _ ), m: ModuleDef ) if mods.hasFlag( CASE ) ⇒
                context.Expr( q"""
                    ${`class`.Class( context )( c )}
                    ${`class`.Companion( context )( c, m )}
                """ )
            case ( _: ClassDef, _ ) ⇒
                sys.error( "Cannot generate Parcelable code for classes, please use case classes instead" )
        }

        annottees.map( _.tree ) match {
            case ( c: ClassDef ) :: Nil ⇒ create( c, q"object ${c.name.toTermName}" )
            case ( m: ModuleDef ) :: Nil ⇒ create( null, m )
            case ( c: ClassDef ) :: ( m: ModuleDef ) :: Nil ⇒ create( c, m )
            case _ ⇒ context.abort( context.enclosingPosition, "Invalid annottee" )
        }
    }
}