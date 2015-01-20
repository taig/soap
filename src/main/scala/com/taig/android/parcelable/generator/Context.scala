package com.taig.android.parcelable.generator

import scala.reflect.macros.{TypecheckException, whitebox}

trait Context[C <: whitebox.Context]
{
	val context: C

	import context.universe._

	implicit class RichTree( tree: Tree )
	{
		def resolveType(): Type =
		{
			val check = tree match
			{
				case AppliedTypeTree( name, parameters ) => q"null.asInstanceOf[$name[..$parameters]]"
				case Apply( name, _ ) => q"null.asInstanceOf[$name]"
				case Ident( name: TypeName ) => q"null.asInstanceOf[$name]"
				case select: Select => q"null.asInstanceOf[$select]"
				case _ => sys.error( s"Can't resolve type:\n${showRaw( tree )}" )
			}

			context.typecheck( check ).tpe
		}
	}

	implicit class RichImplDef( implDef: ImplDef )
	{
		private def getSimpleName( name: String ) = name.replaceFirst( "(?:(?:\\w+\\.)*)(\\w+)(?:\\[.+\\])?", "$1" )

		def extendsFrom[T: TypeTag]: Boolean = implDef.impl.parents.exists( parent =>
		{
			try
			{
				parent.resolveType() <:< typeOf[T]
			}
			catch
			{
				case _: TypecheckException => getSimpleName( parent.toString() ) == getSimpleName( typeOf[T].toString )
			}
		} )

		def hasParent[T: TypeTag]: Boolean = implDef.impl.parents.exists( parent =>
		{
			try
			{
				parent.resolveType() =:= typeOf[T]
			}
			catch
			{
				case _: TypecheckException => getSimpleName( parent.toString() ) == getSimpleName( typeOf[T].toString )
			}
		} )

		def getConstructorFields(): List[ValDef] =
		{
			implDef.impl.body.collect
			{
				case valDef: ValDef if valDef.mods.hasFlag( Flag.PARAMACCESSOR ) => valDef
			}
		}
	}
}