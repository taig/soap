package io.taig.android.parcelable

import scala.reflect.ClassTag

trait Parcelable extends android.os.Parcelable {
    override def describeContents() = 0
}

object Parcelable {
    abstract class Creator[T: ClassTag] extends android.os.Parcelable.Creator[T] {
        override def newArray(size: Int) = new Array[T]( size )
    }
}