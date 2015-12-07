package io.taig.android.parcelable.bundleable

import io.taig.android.parcelable._
import io.taig.android.parcelable.bundleable.Write.fold
import shapeless.Nat._
import shapeless.labelled._
import shapeless.ops.hlist.{ LeftFolder, Length }
import shapeless.ops.nat.ToInt
import shapeless._
import shapeless.syntax.singleton._

import scala.collection.Traversable
import scala.language.higherKinds
import scala.reflect.ClassTag

/**
 * Type class that instructs how to serialize a value to a Bundle
 */
trait Write[-T] {
    def write( value: T ): Bundle
}

trait Write2 {
    implicit def `Write[Bundleize]`[T: bundleize.Write]: Write[T] = Write( Bundle( "value", _ ) )
}

trait Write1 extends Write2 {
    implicit def `Write[LabelledGeneric]`[T, LG](
        implicit
        lg: LabelledGeneric.Aux[T, LG],
        b:  Write[LG]
    ): Write[T] = Write( value ⇒ b.write( lg.to( value ) ) )

}

trait Write0 extends Write1 {
    implicit def `Write[Array]`[T: bundleize.Write]: Write[Array[T]] = Write { value ⇒
        val array = value.zipWithIndex
        val bundle = Bundle( value.length )
        array.foreach{ case ( value, index ) ⇒ bundle.write( index.toString, value ) }
        bundle
    }

    implicit val `Write[CNil]`: Write[CNil] = Write[CNil] { _ ⇒
        sys.error( "No Write representation of CNil (this shouldn't happen)" )
    }

    implicit def `Write[Coproduct]`[K <: Symbol, H, T <: Coproduct](
        implicit
        k: Witness.Aux[K],
        h: Lazy[Write[H]],
        t: Lazy[Write[T]]
    ): Write[FieldType[K, H] :+: T] = Write {
        case Inl( head ) ⇒ Bundle( "type" ->> k.value.name :: "value" ->> h.value.write( head ) :: HNil )
        case Inr( tail ) ⇒ t.value.write( tail )
    }

    implicit def `Write[Either]`[L: bundleize.Write, R: bundleize.Write]: Write[Either[L, R]] = Write {
        case value @ Left( _ )  ⇒ `Write[Left]`[L, R].write( value )
        case value @ Right( _ ) ⇒ `Write[Right]`[L, R].write( value )
    }

    implicit def `Write[HList]`[K <: Symbol, V, T <: HList, N <: Nat](
        implicit
        l:  Length.Aux[FieldType[K, V] :: T, N],
        ti: ToInt[N],
        lf: LeftFolder.Aux[FieldType[K, V] :: T, Bundle, fold.type, Bundle]
    ): Write[FieldType[K, V] :: T] = Write( _.foldLeft( Bundle( toInt[N] ) )( fold ) )

    implicit val `Write[HNil]`: Write[HNil] = Write( _ ⇒ Bundle.empty )

    implicit def `Write[Left]`[L: bundleize.Write, R]: Write[Left[L, R]] = Write {
        case Left( value ) ⇒ Bundle( "either" ->> -1 :: "value" ->> value :: HNil )
    }

    implicit val `Write[None]`: Write[None.type] = Write( _ ⇒ Bundle.empty )

    implicit def `Write[Option]`[T: bundleize.Write]: Write[Option[T]] = Write {
        case Some( value ) ⇒ Bundle( "option", value )
        case None          ⇒ `Write[None]`.write( None )
    }

    implicit def `Write[Right]`[L, R: bundleize.Write]: Write[Right[L, R]] = Write {
        case Right( value ) ⇒ Bundle( "either" ->> 1 :: "value" ->> value :: HNil )
    }

    implicit def `Write[Traversable]`[L[B] <: Traversable[B], T: bundleize.Write: ClassTag]: Write[L[T]] = Write {
        value ⇒ `Write[Array]`[T].write( value.toArray )
    }
}

object Write extends Write0 {
    def apply[T]( f: T ⇒ Bundle ) = new Write[T] {
        override def write( value: T ) = f( value )
    }

    object fold extends Poly2 {
        implicit def default[K <: Symbol, V: bundleize.Write]( implicit key: Witness.Aux[K] ) = {
            at[Bundle, FieldType[K, V]] { ( bundle, value ) ⇒
                implicitly[bundleize.Write[V]].write( bundle, key.value.name, value )
                bundle
            }
        }
    }
}