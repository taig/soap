package io.taig.android.parcelable

import shapeless._
import shapeless.labelled._
import shapeless.ops.hlist.{ Length, LeftFolder }
import shapeless.Nat.toInt
import shapeless.ops.nat.ToInt
import shapeless.syntax.std.tuple._
import shapeless.syntax.singleton._

import scala.collection._
import scala.collection.generic.CanBuildFrom
import scala.reflect.ClassTag

import scala.language.higherKinds

/**
 * Type class that instructs how to deserialize/serialize a value from/to a Bundle
 */
object Bundleable {
    trait Read[T] {
        def read( bundle: Bundle ): T
    }

    trait MinorPriorityRead {
        implicit def `Read[Bundleize]`[T]( implicit r: Lazy[Bundleize.Read[T]] ): Read[T] = {
            Read( _.read[T]( "value" )( r.value ) )
        }
    }

    trait LowPriorityRead extends MinorPriorityRead {
        implicit def `Read[LabelledGeneric]`[T, LG](
            implicit
            lg: LabelledGeneric.Aux[T, LG],
            b:  Read[LG]
        ): Read[T] = Read( bundle ⇒ lg.from( b.read( bundle ) ) )
    }

    trait DefaultPriorityRead extends LowPriorityRead {
        implicit def `Read[Array]`[T: Bundleize.Read: ClassTag]: Read[Array[T]] = Read { bundle ⇒
            import collection.JavaConversions._
            bundle.keySet().map( implicitly[Bundleize.Read[T]].read( bundle, _ ) )( breakOut )
        }

        implicit def `Read[Either]`[A: Bundleize.Read, B: Bundleize.Read]: Read[Either[A, B]] = Read { bundle ⇒
            bundle.read[Int]( "either" ) match {
                case -1 ⇒ `Read[Left]`[A, B].read( bundle )
                case 1  ⇒ `Read[Right]`[A, B].read( bundle )
            }
        }

        implicit def `Read[HList]`[K <: Symbol, V, T <: HList](
            implicit
            key: Witness.Aux[K],
            bv:  Bundleize.Read[V],
            bt:  Read[T]
        ): Read[FieldType[K, V] :: T] = Read(
            bundle ⇒ field[K]( bv.read( bundle, key.value.name ) ) :: bt.read( bundle )
        )

        implicit val `Read[HNil]`: Read[HNil] = Read( _ ⇒ HNil )

        implicit def `Read[Left]`[L: Bundleize.Read, R]: Read[Left[L, R]] = Read { bundle ⇒
            Left( bundle.read[L]( "value" ) )
        }

        implicit def `Read[Option]`[T: Bundleize.Read]: Read[Option[T]] = Read {
            case bundle: Bundle if bundle.containsKey( "option" ) ⇒ `Read[Some]`[T].read( bundle )
            case _ ⇒ None
        }

        implicit def `Read[Right]`[L, R: Bundleize.Read]: Read[Right[L, R]] = Read { bundle ⇒
            Right( bundle.read[R]( "value" ) )
        }

        implicit def `Read[Some]`[T: Bundleize.Read]: Read[Some[T]] = Read { bundle ⇒
            Some( bundle.read[T]( "option" ) )
        }

        implicit def `Read[Traversable]`[L[B] <: Traversable[B], T: Bundleize.Read: ClassTag](
            implicit
            cbf: CanBuildFrom[Nothing, T, L[T]]
        ): Read[L[T]] = Read[L[T]]( `Read[Array]`[T].read( _ ).to[L] )
    }

    object Read extends DefaultPriorityRead {
        def apply[T]( f: Bundle ⇒ T ) = new Read[T] {
            override def read( bundle: Bundle ) = f( bundle )
        }
    }

    trait Write[-T] {
        def write( value: T ): Bundle
    }

    trait MinorPriorityWrite {
        implicit def `Write[Bundleize]`[T]( implicit w: Lazy[Bundleize.Write[T]] ): Write[T] = {
            Write( Bundle( "value", _ )( w.value ) )
        }
    }

    trait LowPriorityWrite extends MinorPriorityWrite {
        implicit def `Write[LabelledGeneric]`[T, LG](
            implicit
            lg: LabelledGeneric.Aux[T, LG],
            b:  Write[LG]
        ): Write[T] = Write( value ⇒ b.write( lg.to( value ) ) )
    }

    trait DefaultPriorityWrite extends LowPriorityWrite {
        implicit def `Write[Array]`[T: Bundleize.Write]: Write[Array[T]] = Write { value ⇒
            val array = value.zipWithIndex
            val bundle = Bundle( value.length )
            array.foreach{ case ( value, index ) ⇒ bundle.write( index.toString, value ) }
            bundle
        }

        implicit def `Write[Either]`[L: Bundleize.Write, R: Bundleize.Write]: Write[Either[L, R]] = Write {
            case value @ Left( _ )  ⇒ `Write[Left]`[L, R].write( value )
            case value @ Right( _ ) ⇒ `Write[Right]`[L, R].write( value )
        }

        implicit def `Write[HList]`[K <: Symbol, V, T <: HList, N <: Nat](
            implicit
            l:  Length.Aux[FieldType[K, V] :: T, N],
            ti: ToInt[N],
            lf: LeftFolder.Aux[FieldType[K, V] :: T, Bundle, fold.write.type, Bundle]
        ): Write[FieldType[K, V] :: T] = Write( _.foldLeft( new Bundle( toInt[N] ) )( fold.write ) )

        implicit val `Write[HNil]`: Write[HNil] = Write( _ ⇒ Bundle.empty )

        implicit def `Write[Left]`[L: Bundleize.Write, R]: Write[Left[L, R]] = Write {
            case Left( value ) ⇒ Bundle( "either" ->> -1 :: "value" ->> value :: HNil )
        }

        implicit val `Write[None]`: Write[None.type] = Write( _ ⇒ Bundle.empty )

        implicit def `Write[Option]`[T: Bundleize.Write]: Write[Option[T]] = Write {
            case Some( value ) ⇒ Bundle( "option", value )
            case None          ⇒ `Write[None]`.write( None )
        }

        implicit def `Write[Right]`[L, R: Bundleize.Write]: Write[Right[L, R]] = Write {
            case Right( value ) ⇒ Bundle( "either" ->> 1 :: "value" ->> value :: HNil )
        }

        implicit def `Write[Traversable]`[L[B] <: Traversable[B], T: Bundleize.Write: ClassTag]: Write[L[T]] = Write {
            value ⇒ `Write[Array]`[T].write( value.toArray )
        }
    }

    object Write extends DefaultPriorityWrite {
        def apply[T]( f: T ⇒ Bundle ) = new Write[T] {
            override def write( value: T ) = f( value )
        }
    }

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
}