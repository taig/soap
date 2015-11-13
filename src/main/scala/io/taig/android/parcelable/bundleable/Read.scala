package io.taig.android.parcelable.bundleable

import io.taig.android.parcelable._
import io.taig.android.parcelable.internal._
import shapeless._
import shapeless.labelled._

import scala.collection._
import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import scala.reflect.ClassTag

/**
 * Type class that instructs how to deserialize a value from a Bundle
 */
trait Read[T] {
    def read( bundle: Bundle ): T
}

trait Read2 {
    implicit def `Read[Bundleize]`[T: bundleize.Read]: Read[T] = Read( _.read[T]( "value" ) )
}

trait Read1 extends Read2 {
    implicit def `Read[LabelledGeneric]`[T, LG](
        implicit
        lg: LabelledGeneric.Aux[T, LG],
        b:  Read[LG]
    ): Read[T] = Read( bundle ⇒ lg.from( b.read( bundle ) ) )
}

trait Read0 extends Read1 {
    implicit def `Read[Array]`[T: bundleize.Read: ClassTag]: Read[Array[T]] = Read { bundle ⇒
        import collection.JavaConversions._
        bundle.keySet().map( implicitly[bundleize.Read[T]].read( bundle, _ ) )( breakOut )
    }

    implicit def `Read[Either]`[A: bundleize.Read, B: bundleize.Read]: Read[Either[A, B]] = Read { bundle ⇒
        bundle.read[Int]( "either" ) match {
            case -1 ⇒ `Read[Left]`[A, B].read( bundle )
            case 1  ⇒ `Read[Right]`[A, B].read( bundle )
        }
    }

    implicit def `Read[HList]`[K <: Symbol, V, T <: HList](
        implicit
        key: Witness.Aux[K],
        bv:  bundleize.Read[V],
        bt:  Read[T]
    ): Read[FieldType[K, V] :: T] = Read(
        bundle ⇒ field[K]( bv.read( bundle, key.value.name ) ) :: bt.read( bundle )
    )

    implicit val `Read[HNil]`: Read[HNil] = Read( _ ⇒ HNil )

    implicit def `Read[Left]`[L: bundleize.Read, R]: Read[Left[L, R]] = Read { bundle ⇒
        Left( bundle.read[L]( "value" ) )
    }

    implicit def `Read[Option]`[T: bundleize.Read]: Read[Option[T]] = Read {
        case bundle: Bundle if bundle.containsKey( "option" ) ⇒ `Read[Some]`[T].read( bundle )
        case _ ⇒ None
    }

    implicit def `Read[Right]`[L, R: bundleize.Read]: Read[Right[L, R]] = Read { bundle ⇒
        Right( bundle.read[R]( "value" ) )
    }

    implicit def `Read[Some]`[T: bundleize.Read]: Read[Some[T]] = Read { bundle ⇒
        Some( bundle.read[T]( "option" ) )
    }

    implicit def `Read[Traversable]`[L[B] <: Traversable[B], T: bundleize.Read: ClassTag](
        implicit
        cbf: CanBuildFrom[Nothing, T, L[T]]
    ): Read[L[T]] = `Read[Array]`[T].map( _.to[L] )
}

object Read extends Read0 {
    def apply[T]( f: Bundle ⇒ T ) = new Read[T] {
        override def read( bundle: Bundle ) = f( bundle )
    }
}