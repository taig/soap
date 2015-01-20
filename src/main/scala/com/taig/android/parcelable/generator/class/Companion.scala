package com.taig.android.parcelable.generator.`class`

import java.io.FileDescriptor

import android.os.{IBinder, Bundle, Parcelable, PersistableBundle}
import android.util.{SparseBooleanArray, Size, SizeF}
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
						override def createFromParcel( source: android.os.Parcel ) = new ${name.toTypeName}(
							..${classDef.getConstructorFields().map( _.tpt ).map( _.resolveType() ).map( read )}
						)

						override def newArray( size: Int ) = new Array[${name.toTypeName}]( size )
					}
					"""
				)
			)
		}
	}

	private def read[T]( `type`: Type ): Tree = `type` match
	{
		case tpe if tpe <:< typeOf[Bundle] => q"source.readBundle()"
		case tpe if tpe <:< typeOf[Boolean] => q"source.readValue( classOf[Boolean].getClassLoader ).asInstanceOf[Boolean]"
		case tpe if tpe <:< typeOf[Byte] => q"source.readByte()"
		case tpe if tpe <:< typeOf[Double] => q"source.readDouble()"
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
		case tpe if tpe <:< typeOf[Option[_]] => q"Option( ${read( tpe.typeArgs.head )} )"
		case tpe if tpe <:< typeOf[Map[_, _]] => q"""
			val keys = { ${read( tq"Iterable[${tpe.typeArgs.head}]".resolveType() )} }
			val values = { ${read( tq"Iterable[${tpe.typeArgs.last}]".resolveType() )} }
			( keys zip values ).toMap
			"""
		case tpe if tpe <:< typeOf[Traversable[Boolean]] || tpe <:< typeOf[Array[Boolean]] =>
		{
			q"source.createBooleanArray().to[${tpe.typeConstructor}]"
		}
		case tpe if tpe <:< typeOf[Traversable[Byte]] || tpe <:< typeOf[Array[Byte]] =>
		{
			q"source.createByteArray().to[${tpe.typeConstructor}]"
		}
		case tpe if tpe <:< typeOf[Traversable[Char]] || tpe <:< typeOf[Array[Char]] =>
		{
			q"source.createCharArray().to[${tpe.typeConstructor}]"
		}
		case tpe if tpe <:< typeOf[Traversable[Double]] || tpe <:< typeOf[Array[Double]] =>
		{
			q"source.createDoubleArray().to[${tpe.typeConstructor}]"
		}
		case tpe if tpe <:< typeOf[Traversable[Float]] || tpe <:< typeOf[Array[Float]] =>
		{
			q"source.createFloatArray().to[${tpe.typeConstructor}]"
		}
		case tpe if tpe <:< typeOf[Traversable[IBinder]] || tpe <:< typeOf[Array[IBinder]] =>
		{
			q"source.createBinderArray().to[${tpe.typeConstructor}]"
		}
		case tpe if tpe <:< typeOf[Traversable[Int]] || tpe <:< typeOf[Array[Int]] =>
		{
			q"source.createIntArray().to[${tpe.typeConstructor}]"
		}
		case tpe if tpe <:< typeOf[Traversable[Long]] || tpe <:< typeOf[Array[Long]] =>
		{
			q"source.createLongArray().to[${tpe.typeConstructor}]"
		}
		case tpe if tpe <:< typeOf[Traversable[String]] || tpe <:< typeOf[Array[String]] =>
		{
			q"source.createStringArray().to[${tpe.typeConstructor}]"
		}
		case tpe if tpe <:< typeOf[Traversable[Parcelable]] || tpe <:< typeOf[Array[Parcelable]] => q"""
			source
				.readParcelableArray( classOf[${tpe.typeArgs.head}].getClassLoader )
				.map( _.asInstanceOf[${tpe.typeArgs.head}] )
				.to[${tpe.typeConstructor}]
			"""
		case tpe if tpe <:< typeOf[Traversable[_]] || tpe <:< typeOf[Array[_]] => q"""
			source
				.readArray( classOf[${tpe.typeArgs.head}].getClassLoader )
				.map( _.asInstanceOf[${tpe.typeArgs.head}] )
				.to[${tpe.typeConstructor}]
			"""
		case tpe if tpe <:< typeOf[Tuple1[_]] => q"Tuple1( ${read( tpe.typeArgs.head )} )"
		case tpe if tpe <:< typeOf[Product] && tpe.typeConstructor.toString.matches( "Tuple\\d+" ) =>
			Apply(
				Ident( TermName( tpe.typeConstructor.toString) ),
				tpe.typeArgs.map( arg => read( arg ) )
			)
		case tpe if tpe <:< typeOf[Serializable] => q"source.readSerializable().asInstanceOf[$tpe]"
		case tpe =>
		{
			context.abort(
				context.enclosingPosition,
				s"No parcel read method available for type $tpe"
			)
		}
	}
}

object Companion
{
	def apply( context: whitebox.Context ) = new Companion[context.type]( context )
}