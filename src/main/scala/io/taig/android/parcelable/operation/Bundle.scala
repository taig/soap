package io.taig.android.parcelable.operation

import scala.reflect.ClassTag

abstract class Bundle( bundle: android.os.Bundle ) {
    def put[T: ClassTag]( key: String, value: T ): Unit = {

    }
}