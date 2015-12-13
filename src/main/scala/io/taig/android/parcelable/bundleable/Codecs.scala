package io.taig.android.parcelable.bundleable

trait Codecs[H] extends LowPriorityCodecs {
    //    implicit def `Codec[Option]`[T]( implicit c: Codec[H, Option[T]] ) = new Codec[H, Option[T]] {
    //        override def encode( host: H, key: String, value: Option[T] ) = ???
    //
    //        override def decode( host: H, key: String ) = ???
    //
    //        override def contains( host: H, key: String ) = false
    //    }
}

trait LowPriorityCodecs