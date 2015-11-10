package io.taig.android.parcelable

import shapeless.labelled._
import shapeless.ops.hlist.{ Length, LeftFolder }
import shapeless._
import shapeless.Nat.toInt
import shapeless.ops.nat.ToInt
import shapeless.syntax.std.tuple._

/**
 * Type class that instructs how to deserialize/serialize a value from/to a Bundle
 */
trait Bundleable[T] {
    def read( bundle: Bundle ): T

    def write( value: T ): Bundle
}

object Bundleable {
    def apply[T]( r: Bundle ⇒ T, w: T ⇒ Bundle ) = new Bundleable[T] {
        override def read( bundle: Bundle ) = r( bundle )

        override def write( value: T ) = w( value )
    }

    def from[T: Bundleable]: Bundleable[T] = the[Bundleable[T]]

    private object fold {
        object write extends Poly2 {
            implicit def default[K <: Symbol, V: Bundleize.Write]( implicit key: Witness.Aux[K] ) = {
                at[Bundle, FieldType[K, V]] { ( bundle, value ) ⇒
                    implicitly[Bundleize.Write[V]].write( bundle, key.value.name, value )
                    bundle
                }
            }
        }
    }

    implicit val `Bundleable[HNil]` = Bundleable[HNil]( _ ⇒ HNil, _ ⇒ Bundle.empty )

    implicit def `Bundleable[HList]`[K <: Symbol, V, T <: HList, N <: Nat](
        implicit
        key: Witness.Aux[K],
        bv:  Bundleize.Read[V],
        bt:  Bundleable[T],
        l:   Length.Aux[FieldType[K, V] :: T, N],
        ti:  ToInt[N],
        lf:  LeftFolder.Aux[FieldType[K, V] :: T, Bundle, fold.write.type, Bundle]
    ) = Bundleable[FieldType[K, V] :: T](
        bundle ⇒ field[K]( bv.read( bundle, key.value.name ) ) :: bt.read( bundle ),
        _.foldLeft( new Bundle( toInt[N] ) )( fold.write )
    )

    implicit def `Bundleable[LabelledGeneric]`[T, LG](
        implicit
        lg: LabelledGeneric.Aux[T, LG],
        b:  Bundleable[LG]
    ) = Bundleable[T]( bundle ⇒ lg.from( b.read( bundle ) ), value ⇒ b.write( lg.to( value ) ) )

    //    implicit def `Bundleable[Bundleize]`[T: Bundleize] = Bundleable[T](
    //        _.read[T]( "value" ),
    //        new Bundle( 1 ).write( "value", _ )
    //    )
}