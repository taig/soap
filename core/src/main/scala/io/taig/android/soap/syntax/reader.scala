package io.taig.android.soap.syntax

import android.content.Intent
import io.taig.android.soap._

import scala.language.implicitConversions

trait reader {
    implicit def readerBundleSyntax( bundle: Bundle ): ops.reader[Bundle] = new ops.reader[Bundle]( bundle )

    implicit def readerIntentSyntax( intent: Intent ): ops.reader[Intent] = new ops.reader[Intent]( intent )
}

object reader extends reader