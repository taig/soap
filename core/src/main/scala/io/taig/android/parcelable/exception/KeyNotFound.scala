package io.taig.android.parcelable.exception

case class KeyNotFound( key: String ) extends IllegalStateException( s"Key '$key' does not exist" )