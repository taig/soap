package com.taig.android.parcelable

trait Creator[T]
{
	def CREATOR: android.os.Parcelable.Creator[T]
}