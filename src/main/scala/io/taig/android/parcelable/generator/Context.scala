package io.taig.android.parcelable.generator

import scala.reflect.macros.{TypecheckException, whitebox}

trait Context[C <: whitebox.Context]
{
	val context: C

	import context.universe._

	implicit class RichClassDef( classDef: ClassDef )
	{
		/**
		 * Retrieve the name, with type arguments
		 */
		def getFullName(): Tree =
		{
			def discover( name: TypeName, types: List[TypeDef] ): Tree = ( name, types ) match
			{
				case ( name: TypeName, Nil ) => tq"$name"
				case ( name: TypeName, types ) =>
				{
					tq"$name[..${types.map{ case TypeDef( _, name, types, _ ) => discover( name, types ) }}]"
				}
			}

			discover( classDef.name, classDef.tparams )
		}

		/**
		 * Retrieve the name, type arguments replaced with wildcards
		 */
		def getWildcardedName(): Tree = classDef match
		{
			case ClassDef( _, name, Nil, _ ) => tq"$name"
			case ClassDef( _, name, types, _ ) => ExistentialTypeTree(
				AppliedTypeTree(
					Ident( name ),
					( 1 to types.length ).map( i => Ident( TypeName( "_$" + i ) ) ).toList
				),
				( 1 to types.length ).map( i =>
				{
					TypeDef(
						Modifiers( Flag.DEFERRED | Flag.SYNTHETIC ),
						TypeName( "_$" + i ),
						List.empty,
						TypeBoundsTree( EmptyTree, EmptyTree )
					)
				} ).toList
			)
		}
	}

	implicit class RichClassSymbol( classSymbol: ClassSymbol )
	{
		def directBaseClasses() =
		{
			val base = classSymbol.baseClasses.toSet - classSymbol
			val basebase = base.flatMap{ case x: ClassSymbol => x.baseClasses.toSet - x }

			base -- basebase
		}
	}

	implicit class RichImplDef( implDef: ImplDef )
	{
		private def getSimpleName( name: String ) = name.replaceFirst( "(?:(?:\\w+\\.)*)(\\w+)(?:\\[.+\\])?", "$1" )

		/**
		 * Does this class extend from T (directly or through its parents)?
		 * 
		 * @see hasParent
		 */
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

		/**
		 * Does this class directly extend from T?
		 * 
		 * @see extendsFrom
		 */
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

		/**
		 * Retrieve all fields that are flagged with [[Flag.PARAMACCESSOR]]
		 */
		def getConstructorFields(): List[ValDef] =
		{
			implDef.impl.body.collect
			{
				case valDef: ValDef if valDef.mods.hasFlag( Flag.PARAMACCESSOR ) => valDef
			}
		}

		/**
		 * Retrieve all methods are named as [[termNames.CONSTRUCTOR]]
		 */
		def getConstructors(): List[DefDef] =
		{
			implDef.impl.body.collect
			{
				case defDef: DefDef if defDef.name == termNames.CONSTRUCTOR => defDef
			}
		}

		/**
		 * Retrieve the head of [[getConstructors()]]
		 */
		def getPrimaryConstructor(): DefDef = getConstructors().head
	}

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

	implicit class RichType( `type`: Type )
	{
		def is[T: TypeTag] = `type` <:< typeOf[T]

		/**
		 * Check if this Type is a child of Traversable[T] or Array[T] 
		 */
		def isCollection[T: TypeTag] =
		{
			`type` <:< typeOf[Traversable[T]] || ( `type` <:< typeOf[Array[_]] && `type`.typeArgs.head <:< typeOf[T] )
		}
	}
}