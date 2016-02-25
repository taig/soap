package io.taig.android.soap.syntax

import io.taig.android.soap.Bundle
import io.taig.android.soap.ops

import scala.language.implicitConversions

trait bundle {
    implicit def bundleSyntax( bundle: Bundle ): ops.bundle = new ops.bundle( bundle )
}

object bundle extends bundle