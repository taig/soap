package io.taig.android.soap.operation

import android.content.{ Intent, SharedPreferences }
import android.os.Bundle
import io.circe.Decoder
import io.circe.parser._

sealed trait reader[C] {
    def container: C

    def read[V: Decoder]( key: String ): Option[V]
}

object reader {
    @inline
    private def parse[V: Decoder]( json: String ): Option[V] = {
        Option( json ).flatMap { json ⇒
            decode[V]( json ).toOption
        }
    }

    final case class bundle( container: Bundle ) extends reader[Bundle] {
        override def read[V: Decoder]( key: String ): Option[V] = {
            parse( container.getString( key ) )
        }
    }

    final case class intent( container: Intent ) extends reader[Intent] {
        override def read[V: Decoder]( key: String ): Option[V] = {
            parse( container.getStringExtra( key ) )
        }
    }

    final case class sharedPreferences( container: SharedPreferences )
            extends reader[SharedPreferences] {
        override def read[V: Decoder]( key: String ): Option[V] = {
            parse( container.getString( key, null ) )
        }
    }
}