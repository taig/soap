package io.taig.android.parcelable.syntax

import io.taig.android.parcelable.Bundle
import io.taig.android.parcelable.ops

import scala.language.implicitConversions

trait bundle {
    implicit def bundleSyntax( bundle: Bundle ): ops.bundle = new ops.bundle( bundle )
}

object bundle extends bundle