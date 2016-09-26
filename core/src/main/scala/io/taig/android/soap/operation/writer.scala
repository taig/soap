package io.taig.android.soap.operation

import android.content.{ Intent, SharedPreferences }
import io.circe.Encoder
import io.circe.syntax._
import io.taig.android.soap.Bundle
import io.taig.android.soap.operation.writer.fold
import shapeless.labelled._
import shapeless.ops.hlist.LeftFolder
import shapeless.{ HList, Lazy, Poly2, Witness }

sealed trait writer[C] {
    def container: C

    def write[V: Encoder]( key: String, value: V ): C

    def writeAll[L <: HList]( arguments: L )( implicit lf: fold.F[L, C] ): C = {
        arguments.foldLeft( this )( fold )
        container
    }
}

object writer {
    object fold extends Poly2 {
        type F[L <: HList, C] = LeftFolder.Aux[L, writer[C], this.type, writer[C]]

        implicit def default[C, K, V](
            implicit
            k: Witness.Aux[K],
            e: Lazy[Encoder[V]]
        ): Case.Aux[writer[C], FieldType[K, V], writer[C]] = at {
            case ( writer, value ) ⇒
                val key = k.value match {
                    case symbol: Symbol ⇒ symbol.name
                    case any            ⇒ any.toString
                }

                writer.write[V]( key, value )( e.value )
                writer
        }
    }

    final case class bundle( container: Bundle ) extends writer[Bundle] {
        override def write[V: Encoder]( key: String, value: V ): Bundle = {
            container.putString( key, value.asJson.noSpaces )
            container
        }
    }

    final case class intent( container: Intent ) extends writer[Intent] {
        override def write[V: Encoder]( key: String, value: V ): Intent = {
            container.putExtra( key, value.asJson.noSpaces )
            container
        }
    }

    final case class sharedPreferences( container: SharedPreferences )
            extends writer[SharedPreferences] {
        override def write[V: Encoder]( key: String, value: V ): SharedPreferences = {
            container
                .edit()
                .putString( key, value.asJson.noSpaces )
                .commit()
            container
        }
    }
}