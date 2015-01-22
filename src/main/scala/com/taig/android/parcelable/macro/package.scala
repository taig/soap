package com.taig.android.parcelable

import scala.reflect.macros.whitebox

package object `macro`
{
	def parcelable( context: whitebox.Context )( annottees: context.Expr[Any]* ): context.Expr[Any] =
	{
		import context.universe.Flag._
		import context.universe._

		def create( c: ClassDef, m: ModuleDef ): Expr[Any] = ( c, m ) match
		{
			/*
			 * Object
			 */
			case ( null, m: ModuleDef ) =>
			{
				context.Expr( q"""
					${generator.`object`.Class( context )( m )}
					${generator.`object`.Companion( context )( m )}
					"""
				)
			}
			/*
			 * Abstract Class, Trait
			 */
			case ( c @ ClassDef( mods, _, _, _ ), m: ModuleDef ) if mods.hasFlag( ABSTRACT ) =>
			{
				context.Expr( q"""
					${generator.`abstract`.Class( context )( c )}
					${generator.`abstract`.Companion( context )( c, m )}
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
				println( show( x.tree ) )
				x
			}
		}

		annottees.map( _.tree ) match
		{
			case ( c: ClassDef ) :: Nil => create( c, q"object ${c.name.toTermName}" )
			case ( m: ModuleDef ) :: Nil => create( null, m )
			case ( c: ClassDef ) :: ( m: ModuleDef ) :: Nil => create( c, m )
			case _ => context.abort( context.enclosingPosition, "Invalid annottee" )
		}
	}
}