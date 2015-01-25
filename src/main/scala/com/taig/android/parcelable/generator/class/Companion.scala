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
		case tpe if tpe.is[Bundle] => q"source.readBundle()"
		case tpe if tpe.is[Boolean] => q"source.readValue( classOf[Boolean].getClassLoader ).asInstanceOf[Boolean]"
		case tpe if tpe.is[Byte] => q"source.readByte()"
		case tpe if tpe.is[Double] => q"source.readDouble()"
		case tpe if tpe.is[IBinder] => q"source.readStrongBinder()"
		case tpe if tpe.is[FileDescriptor] => q"source.readFileDescriptor()"
		case tpe if tpe.is[Float] => q"source.readFloat()"
		case tpe if tpe.is[Int] => q"source.readInt()"
		case tpe if tpe.is[Long] => q"source.readLong()"
		case tpe if tpe.is[Parcelable] => q"source.readParcelable[$tpe]( classOf[$tpe].getClassLoader )"
		case tpe if tpe.is[PersistableBundle] => q"source.readPersistableBundle()"
		case tpe if tpe.is[Short] => q"source.readValue( classOf[Short].getClassLoader ).asInstanceOf[Short]"
		case tpe if tpe.is[Size] => q"source.readSize()"
		case tpe if tpe.is[SizeF] => q"source.readSizeF()"
		case tpe if tpe.is[String] => q"source.readString()"
		case tpe if tpe.is[CharSequence] =>
		{
			q"android.text.TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel( source ).asInstanceOf[$tpe]"
		}
		case tpe if tpe.is[SparseBooleanArray] => q"source.readSparseBooleanArray()"
		case tpe if tpe.is[Option[_]] =>
		{
			q"""
			source.readInt() match
			{
				case 1 => Some( ${read( tpe.typeArgs.head )} )
				case -1 => None
			}
			"""
		}
		case tpe if tpe.is[Map[_, _]] =>
		{
			q"""
			val keys = ${read( tq"Iterable[${tpe.typeArgs.head}]".resolveType() )}
			val values = ${read( tq"Iterable[${tpe.typeArgs.last}]".resolveType() )}
			( keys zip values ).toMap
			"""
		}
		case tpe if tpe.isCollection[Boolean] => q"source.createBooleanArray().to[${tpe.typeConstructor}]"
		case tpe if tpe.isCollection[Byte] => q"source.createByteArray().to[${tpe.typeConstructor}]"
		case tpe if tpe.isCollection[Char] => q"source.createCharArray().to[${tpe.typeConstructor}]"
		case tpe if tpe.isCollection[Double] => q"source.createDoubleArray().to[${tpe.typeConstructor}]"
		case tpe if tpe.isCollection[Float] => q"source.createFloatArray().to[${tpe.typeConstructor}]"
		case tpe if tpe.isCollection[IBinder] => q"source.createBinderArray().to[${tpe.typeConstructor}]"
		case tpe if tpe.isCollection[Int] => q"source.createIntArray().to[${tpe.typeConstructor}]"
		case tpe if tpe.isCollection[Long] => q"source.createLongArray().to[${tpe.typeConstructor}]"
		case tpe if tpe.isCollection[Parcelable] => q"""
			source
				.readParcelableArray( classOf[${tpe.typeArgs.head}].getClassLoader )
				.map( _.asInstanceOf[${tpe.typeArgs.head}] )
				.to[${tpe.typeConstructor}]
			"""
		case tpe if tpe.isCollection[String] => q"source.createStringArray().to[${tpe.typeConstructor}]"
		case tpe if tpe.is[Product] && tpe.typeConstructor.toString.matches( "Tuple\\d+" ) =>
			Apply(
				Ident( TermName( tpe.typeConstructor.toString) ),
				tpe.typeArgs.map( arg => read( arg ) )
			)
		case tpe if tpe.is[Serializable] =>
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