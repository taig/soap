package io.taig.android.parcelable.generator

trait Creator[T] {
    def CREATOR: android.os.Parcelable.Creator[T]
}