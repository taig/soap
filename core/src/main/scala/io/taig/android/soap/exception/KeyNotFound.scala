package io.taig.android.soap.exception

case class KeyNotFound( key: String, host: String )
    extends IllegalStateException( s"Key '$key' does not exist in:\n$host" )