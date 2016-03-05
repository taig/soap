package io.taig.android.soap.syntax

import android.content.Intent
import io.taig.android.soap._

import scala.language.implicitConversions

trait writer {
    implicit def writerBundleSyntax( bundle: Bundle ): ops.writer[Bundle] = new ops.writer[Bundle]( bundle )

    implicit def writerIntentSyntax( intent: Intent ): ops.writer[Intent] = new ops.writer[Intent]( intent )
}

object writer extends writer