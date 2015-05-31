package io.taig.android.parcelable.generator.`class`

import io.taig.android.parcelable.generator.Context

import scala.reflect.macros.whitebox

class	Class[C <: whitebox.Context]( val context: C )
extends	Context[C]
{
	import context.universe._

	def apply( classDef: context.universe.ClassDef ) =
	{
		val ClassDef( modifiers, name, types, Template( parents, self, body ) ) = classDef

		if( classDef.hasParent[android.os.Parcelable] )
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
								case ValDef( _, name, tpe, _ ) =>
								{
									q"implicitly[io.taig.android.parcelable.Writer[$tpe]].apply( $name, destination )"
								}
							}
						}
					}"""
				)
			)
		}
	}

//	private def write( `type`: Type, name: Tree ): Tree = `type` match
//	{
//		case tpe if tpe.is[Bundle] => q"destination.writeBundle( $name )"
//		case tpe if tpe.is[Boolean] => q"destination.writeValue( $name )"
//		case tpe if tpe.is[Byte] => q"destination.writeByte( $name )"
//		case tpe if tpe.is[Double] => q"destination.writeDouble( $name )"
//		case tpe if tpe.is[IBinder] => q"destination.writeStrongBinder( $name )"
//		case tpe if tpe.is[FileDescriptor] => q"destination.writeFileDescriptor( $name )"
//		case tpe if tpe.is[Float] => q"destination.writeFloat( $name )"
//		case tpe if tpe.is[Int] => q"destination.writeInt( $name )"
//		case tpe if tpe.is[Long] => q"destination.writeLong( $name )"
//		case tpe if tpe.is[Parcelable] => q"destination.writeParcelable( $name, flags )"
//		case tpe if tpe.is[PersistableBundle] => q"destination.writePersistableBundle( $name )"
//		case tpe if tpe.is[Short] => q"destination.writeValue( $name )"
//		case tpe if tpe.is[Size] => q"destination.writeSize( $name )"
//		case tpe if tpe.is[SizeF] => q"destination.writeSizeF( $name )"
//		case tpe if tpe.is[String] => q"destination.writeString( $name )"
//		case tpe if tpe.is[CharSequence] => q"android.text.TextUtils.writeToParcel( $name, destination, flags )"
//		case tpe if tpe.is[SparseBooleanArray] => q"destination.writeSparseBooleanArray( $name )"
//		case tpe if tpe.is[Option[_]] =>
//		{
//			val x = TermName( context.freshName() )
//
//			q"""
//			$name match
//			{
//				case Some( $x ) =>
//				{
//					destination.writeInt( 1 )
//					${write( tpe.typeArgs.head, q"$x" )}
//				}
//				case None => destination.writeInt( -1 )
//			}
//			"""
//		}
//		case tpe if tpe.is[Map[_, _]] =>
//		{
//			q"""
//			${write( tq"Iterable[${tpe.typeArgs.head}]".resolveType(), q"$name.keys" )}
//			${write( tq"Iterable[${tpe.typeArgs.last}]".resolveType(), q"$name.values" )}
//			"""
//		}
//		case tpe if tpe.isCollection[Boolean] => q"destination.writeBooleanArray( $name.toArray )"
//		case tpe if tpe.isCollection[Byte] => q"destination.writeByteArray( $name.toArray )"
//		case tpe if tpe.isCollection[Char] => q"destination.writeCharArray( $name.toArray )"
//		case tpe if tpe.isCollection[Double] => q"destination.writeDoubleArray( $name.toArray )"
//		case tpe if tpe.isCollection[Float] => q"destination.writeFloatArray( $name.toArray )"
//		case tpe if tpe.isCollection[IBinder] => q"destination.writeBinderArray( $name.toArray )"
//		case tpe if tpe.isCollection[Int] => q"destination.writeIntArray( $name.toArray )"
//		case tpe if tpe.isCollection[Long] => q"destination.writeLongArray( $name.toArray )"
//		case tpe if tpe.isCollection[Parcelable] => q"destination.writeParcelableArray( $name.toArray, flags )"
//		case tpe if tpe.isCollection[String] => q"destination.writeStringArray( $name.toArray )"
//		case tpe if tpe.is[Product] && tpe.typeConstructor.toString.matches( "Tuple\\d+" ) =>
//		{
//			q"..${
//				tpe
//					.typeArgs
//					.zipWithIndex
//					.map{ case ( arg, i ) => write( arg, Select( name, TermName( "_" + ( i + 1 ) ) ) ) }
//			}"
//		}
//		case tpe if tpe.is[Serializable] => q"destination.writeSerializable( $name )"
//		case tpe =>
//		{
//			context.abort(
//				context.enclosingPosition,
//				s"No parcel write method available for type $tpe"
//			)
//		}
//	}
}

object Class
{
	def apply( context: whitebox.Context ) = new Class[context.type]( context )
}