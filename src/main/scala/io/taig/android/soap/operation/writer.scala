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