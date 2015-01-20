package com.taig.android.parcelable.generator.`class`

import java.io.FileDescriptor

import android.os._
import android.util.{SparseBooleanArray, Size, SizeF}
import com.taig.android.parcelable.generator.Context

import scala.reflect.macros.whitebox

class	Class[C <: whitebox.Context]( val context: C )
extends	Context[C]
{
	import context.universe._

	def apply( classDef: context.universe.ClassDef ) =
	{
		val ClassDef( modifiers, name, types, Template( parents, self, body ) ) = classDef

		if( classDef.extendsFrom[android.os.Parcelable] )
		{
			classDef
		}
		else
		{
			ClassDef(
				modifiers,
				name,
				types,
				Template(
					parents :+ tq"android.os.Parcelable",
					self,
					body :+
					q"override def describeContents(): Int = 0" :+
					q"""
					override def writeToParcel( destination: android.os.Parcel, flags: Int ): Unit =
					{
						..${
							classDef.getConstructorFields().map
							{
								case ValDef( _, name, tpe, _ ) => write( tpe.resolveType(), q"$name" )
							}
						}
					}"""
				)
			)
		}
	}

	private def write( `type`: Type, name: Tree ): Tree = `type` match
	{
		case tpe if tpe <:< typeOf[Bundle] => q"destination.writeBundle( $name )"
		case tpe if tpe <:< typeOf[Boolean] => q"destination.writeValue( $name )"
		case tpe if tpe <:< typeOf[Byte] => q"destination.writeByte( $name )"
		case tpe if tpe <:< typeOf[Double] => q"destination.writeDouble( $name )"
		case tpe if tpe <:< typeOf[IBinder] => q"destination.writeStrongBinder( $name )"
		case tpe if tpe <:< typeOf[FileDescriptor] => q"destination.writeFileDescriptor( $name )"
		case tpe if tpe <:< typeOf[Float] => q"destination.writeFloat( $name )"
		case tpe if tpe <:< typeOf[Int] => q"destination.writeInt( $name )"
		case tpe if tpe <:< typeOf[Long] => q"destination.writeLong( $name )"
		case tpe if tpe <:< typeOf[Parcelable] => q"destination.writeParcelable( $name, flags )"
		case tpe if tpe <:< typeOf[PersistableBundle] => q"destination.writePersistableBundle( $name )"
		case tpe if tpe <:< typeOf[Short] => q"destination.writeValue( $name )"
		case tpe if tpe <:< typeOf[Size] => q"destination.writeSize( $name )"
		case tpe if tpe <:< typeOf[SizeF] => q"destination.writeSizeF( $name )"
		case tpe if tpe <:< typeOf[String] => q"destination.writeString( $name )"
		case tpe if tpe <:< typeOf[CharSequence] => q"android.text.TextUtils.writeToParcel( $name, destination, flags )"
		case tpe if tpe <:< typeOf[SparseBooleanArray] => q"destination.writeSparseBooleanArray( $name )"
		case tpe if tpe <:< typeOf[Option[_]] =>
		{
			write( tpe.typeArgs.head, q"$name.getOrElse( null ).asInstanceOf[${tpe.typeArgs.head}]" )
		}
		case tpe if tpe <:< typeOf[Map[_, _]] =>
		{
			q"""
			${write( tq"Iterable[${tpe.typeArgs.head}]".resolveType(), q"$name.keys" )}
			${write( tq"Iterable[${tpe.typeArgs.last}]".resolveType(), q"$name.values" )}
			"""
		}
		case tpe if tpe <:< typeOf[Traversable[Boolean]] || tpe <:< typeOf[Array[Boolean]] =>
		{
			q"destination.writeBooleanArray( $name.toArray )"
		}
		case tpe if tpe <:< typeOf[Traversable[Byte]] || tpe <:< typeOf[Array[Byte]] =>
		{
			q"destination.writeByteArray( $name.toArray )"
		}
		case tpe if tpe <:< typeOf[Traversable[Char]] || tpe <:< typeOf[Array[Char]] =>
		{
			q"destination.writeCharArray( $name.toArray )"
		}
		case tpe if tpe <:< typeOf[Traversable[Double]] || tpe <:< typeOf[Array[Double]] =>
		{
			q"destination.writeDoubleArray( $name.toArray )"
		}
		case tpe if tpe <:< typeOf[Traversable[Float]] || tpe <:< typeOf[Array[Float]] =>
		{
			q"destination.writeFloatArray( $name.toArray )"
		}
		case tpe if tpe <:< typeOf[Traversable[IBinder]] || tpe <:< typeOf[Array[IBinder]] =>
		{
			q"destination.writeBinderArray( $name.toArray )"
		}
		case tpe if tpe <:< typeOf[Traversable[Int]] || tpe <:< typeOf[Array[Int]] =>
		{
			q"destination.writeIntArray( $name.toArray )"
		}
		case tpe if tpe <:< typeOf[Traversable[Parcelable]] || tpe <:< typeOf[Array[Parcelable]] =>
		{
			q"destination.writeParcelableArray( $name.toArray, flags )"
		}
		case tpe if tpe <:< typeOf[Traversable[Long]] || tpe <:< typeOf[Array[Long]] =>
		{
			q"destination.writeLongArray( $name.toArray )"
		}
		case tpe if tpe <:< typeOf[Traversable[String]] || tpe <:< typeOf[Array[String]] =>
		{
			q"destination.writeStringArray( $name.toArray )"
		}
		case tpe if tpe <:< typeOf[Traversable[_]] || tpe <:< typeOf[Array[_]] =>
		{
			q"destination.writeArray( $name.toArray )"
		}
		case tpe if tpe <:< typeOf[Tuple1[_]] => write( tpe.typeArgs.head, q"$name._1" )
		case tpe if tpe <:< typeOf[Product] && tpe.typeConstructor.toString.matches( "Tuple\\d+" ) =>
		{
			q"..${
				tpe
					.typeArgs
					.zipWithIndex
					.map{ case ( arg, i ) => write( arg, Select( name, TermName( "_" + ( i + 1 ) ) ) ) }
			}"
		}
		case tpe if tpe <:< typeOf[Serializable] => q"destination.writeSerializable( $name )"
		case tpe =>
		{
			context.abort(
				context.enclosingPosition,
				s"No parcel write method available for type $tpe"
			)
		}
	}
}

object Class
{
	def apply( context: whitebox.Context ) = new Class[context.type]( context )
}