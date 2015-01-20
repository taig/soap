package com.taig.android.parcelable

import scala.reflect.macros.whitebox

package object `macro`
{
	def parcelable( context: whitebox.Context )( annottees: context.Expr[Any]* ): context.Expr[Any] =
	{
		import context.universe._
		import context.universe.Flag._

		def create( c: ClassDef, m: ModuleDef ): Expr[Any] = ( c, m ) match
		{
			/*
			 * Abstract Class, Trait
			 */
			case ( c @ ClassDef( mods, _, _, _ ), m: ModuleDef ) if mods.hasFlag( ABSTRACT ) =>
			{
				context.Expr( q"""
					${generator.`abstract`.Class( context )( c )}
					${generator.`abstract`.Companion( context )( m )}
					"""
				)
			}
			/*
			 * Class, Case Class
			 */
			case ( c: ClassDef, m: ModuleDef ) =>
			{
				val x = context.Expr( q"""
					${generator.`class`.Class( context )( c )}
					${generator.`class`.Companion( context )( c, m )}
					"""
				)
				println( show( x ) )
				x
			}
		}

		annottees.map( _.tree ) match
		{
			case ( c: ClassDef ) :: Nil => create( c, q"object ${c.name.toTermName}" )
			case ( c: ClassDef ) :: ( m: ModuleDef ) :: Nil => create( c, m )
			case _ => context.abort( context.enclosingPosition, "Invalid annottee" )
		}
	}
}