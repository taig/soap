package io.taig.android.soap.operation

import android.content.{ Intent, SharedPreferences }
import io.circe.Encoder
import io.circe.syntax._
import io.taig.android.soap.Bundle

sealed trait writer[C] {
    def container: C

    def write[V: Encoder]( key: String, value: V ): C
}

object writer {
    @inline
    private def encode[V: Encoder]( value: V )( f: String ⇒ Unit ): Unit = {
        f( value.asJson.noSpaces )
    }

    final case class bundle( container: Bundle ) extends writer[Bundle] {
        override def write[V: Encoder]( key: String, value: V ): Bundle = {
            encode( value )( container.putString( key, _ ) )
            container
        }
    }

    final case class intent( container: Intent ) extends writer[Intent] {
        override def write[V: Encoder]( key: String, value: V ): Intent = {
            encode( value )( container.putExtra( key, _ ) )
            container
        }
    }

    final case class sharedPreferences( container: SharedPreferences )
            extends writer[SharedPreferences] {
        override def write[V: Encoder]( key: String, value: V ): SharedPreferences = {
            encode( value ) {
                container
                    .edit()
                    .putString( key, _ )
                    .commit()
            }
            container
        }
    }
}