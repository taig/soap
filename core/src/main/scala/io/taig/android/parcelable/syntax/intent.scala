package io.taig.android.parcelable.syntax

import android.content.Intent
import io.taig.android.parcelable.ops

import scala.language.implicitConversions

trait intent {
    implicit def intentSyntax( intent: Intent ): ops.intent = new ops.intent( intent )
}

object intent extends intent