package io.taig.android.soap

trait Decoder[I, O] {
    def decode( serialization: I ): O
}

object Decoder {
    trait Guarded[I, O] extends Decoder[( I, String ), O] {
        override def decode( serialization: ( I, String ) ) = serialization match {
            case ( host, key ) ⇒ contains( host, key ) match {
                case true  ⇒ decodeRaw( serialization )
                case false ⇒ throw exception.KeyNotFound( key, printHost( host ) )
            }
        }

        protected def printHost( host: I ): String

        protected def contains( host: I, key: String ): Boolean

        protected def decodeRaw( serialization: ( I, String ) ): O
    }
}