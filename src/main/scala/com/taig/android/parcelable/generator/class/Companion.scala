package com.taig.android.parcelable.generator.`class`

import java.io.FileDescriptor

import android.os.{Bundle, IBinder, Parcelable, PersistableBundle}
import android.util.{Size, SizeF, SparseBooleanArray}
import com.taig.android.parcelable.generator.Context

import scala.reflect.macros.whitebox

class	Companion[C <: whitebox.Context]( val context: C )
extends	Context[C]
{
	import context.universe._

	type T[+X] = Traversable[X]

	type A[X] = Array[X]

	def apply( classDef: ClassDef, moduleDef: ModuleDef ) =
	{
		val ModuleDef( modifiers, name, Template( parents, self, body ) ) = moduleDef

		if( moduleDef.extendsFrom[com.taig.android.parcelable.Creator[_]] )
		{
			moduleDef
		}
		else
		{
			ModuleDef(
				modifiers,
				name,
				Template(
					parents :+ tq"com.taig.android.parcelable.Creator[${name.toTypeName}]",
					self,
					body :+
					q"""
					override lazy val CREATOR = new android.os.Parcelable.Creator[${name.toTypeName}]
					{
						override def createFromParcel( source: android.os.Parcel ) = ${instantiate( classDef )}

						override def newArray( size: Int ) = new Array[${name.toTypeName}]( size )
					}
					"""
				)
			)
		}
	}

	private def read( `type`: Type ): Tree = `type` match
	{
		case tpe if tpe <:< typeOf[Bundle] => q"source.readBundle()"
		case tpe if tpe <:< typeOf[Boolean] => q"source.readValue( classOf[Boolean].getClassLoader ).asInstanceOf[Boolean]"
		case tpe if tpe <:< typeOf[Byte] => q"source.readByte()"
		case tpe if tpe <:< typeOf[Double] => q"source.readDouble()"
		case tpe if tpe <:< typeOf[IBinder] => q"source.readStrongBinder()"
		case tpe if tpe <:< typeOf[FileDescriptor] => q"source.readFileDescriptor()"
		case tpe if tpe <:< typeOf[Float] => q"source.readFloat()"
		case tpe if tpe <:< typeOf[Int] => q"source.readInt()"
		case tpe if tpe <:< typeOf[Long] => q"source.readLong()"
		case tpe if tpe <:< typeOf[Parcelable] => q"source.readParcelable[$tpe]( classOf[$tpe].getClassLoader )"
		case tpe if tpe <:< typeOf[PersistableBundle] => q"source.readPersistableBundle()"
		case tpe if tpe <:< typeOf[Short] => q"source.readValue( classOf[Short].getClassLoader ).asInstanceOf[Short]"
		case tpe if tpe <:< typeOf[Size] => q"source.readSize()"
		case tpe if tpe <:< typeOf[SizeF] => q"source.readSizeF()"
		case tpe if tpe <:< typeOf[String] => q"source.readString()"
		case tpe if tpe <:< typeOf[CharSequence] =>
		{
			q"android.text.TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel( source ).asInstanceOf[$tpe]"
		}
		case tpe if tpe <:< typeOf[SparseBooleanArray] => q"source.readSparseBooleanArray()"
		case tpe if tpe <:< typeOf[Option[_]] =>
		{
			q"""
			source.readInt() match
			{
				case 1 => Some( ${read( tpe.typeArgs.head )} )
				case -1 => None
			}
			"""
		}
		case tpe if tpe <:< typeOf[Map[_, _]] =>
		{
			q"""
			val keys = ${read( tq"Iterable[${tpe.typeArgs.head}]".resolveType() )}
			val values = ${read( tq"Iterable[${tpe.typeArgs.last}]".resolveType() )}
			( keys zip values ).toMap
			"""
		}
		case tpe if tpe <:< typeOf[T[_]] || tpe <:< typeOf[A[_]] => q"${collection( tpe )}.to[${tpe.typeConstructor}]"
		case tpe if tpe <:< typeOf[Product] && tpe.typeConstructor.toString.matches( "Tuple\\d+" ) =>
			Apply(
				Ident( TermName( tpe.typeConstructor.toString) ),
				tpe.typeArgs.map( arg => read( arg ) )
			)
		case tpe if tpe <:< typeOf[Serializable] =>
		{
			if( !tpe.typeSymbol.asClass.directBaseClasses().contains( typeOf[Serializable].typeSymbol ) )
			{
				println( s"Notice: Treating type $tpe as Serializable. Please make sure this behavior is intended!" )
			}

			q"source.readSerializable().asInstanceOf[$tpe]"
		}
		case tpe =>
		{
			context.abort(
				context.enclosingPosition,
				s"No parcel read method available for type $tpe"
			)
		}
	}

	private def collection( `type`: Type ) = `type` match
	{
		case tpe if tpe <:< typeOf[T[Boolean]] || tpe <:< typeOf[A[Boolean]] => q"source.createBooleanArray()"
		case tpe if tpe <:< typeOf[T[Byte]] || tpe <:< typeOf[A[Byte]] => q"source.createByteArray()"
		case tpe if tpe <:< typeOf[T[Char]] || tpe <:< typeOf[A[Char]] => q"source.createCharArray()"
		case tpe if tpe <:< typeOf[T[Double]] || tpe <:< typeOf[A[Double]] => q"source.createDoubleArray()"
		case tpe if tpe <:< typeOf[T[Float]] || tpe <:< typeOf[A[Float]] => q"source.createFloatArray()"
		case tpe if tpe <:< typeOf[T[IBinder]] || tpe <:< typeOf[A[IBinder]] => q"source.createBinderArray()"
		case tpe if tpe <:< typeOf[T[Int]] || tpe <:< typeOf[A[Int]] => q"source.createIntArray()"
		case tpe if tpe <:< typeOf[T[Long]] || tpe <:< typeOf[A[Long]] => q"source.createLongArray()"
		case tpe if tpe <:< typeOf[T[String]] || tpe <:< typeOf[A[String]] => q"source.createStringArray()"
		case tpe if tpe <:< typeOf[T[Parcelable]] || tpe <:< typeOf[A[Parcelable]] => q"""
			source
				.readParcelableArray( classOf[${tpe.typeArgs.head}].getClassLoader )
				.map( _.asInstanceOf[${tpe.typeArgs.head}] )
			"""
		case tpe if tpe <:< typeOf[T[_]] || tpe <:< typeOf[A[_]] => q"""
			source
				.readArray( classOf[${tpe.typeArgs.head}].getClassLoader )
				.map( _.asInstanceOf[${tpe.typeArgs.head}] )
			"""
	}

//	private def map( `type`: Type ) = `type` match
//	{
//		case tpe if tpe <:< typeOf[Map[_, _]] => 
//	}

	private def instantiate( classDef: ClassDef ) =
	{
		def construct( reads: List[List[Tree]] ): Apply = reads match
		{
			case List( read ) => Apply( Select( New( Ident( classDef.name ) ), termNames.CONSTRUCTOR ), read )
			case read :: reads => Apply( construct( reads ), read )
			case Nil => construct( List( List.empty ) )
		}

		val reads = classDef
			.getPrimaryConstructor()
			.vparamss
			.map( _.map( _.tpt.resolveType() ) )
			.map( _.map( read ) )

		construct( reads.reverse )
	}
}

object Companion
{
	def apply( context: whitebox.Context ) = new Companion[context.type]( context )
}