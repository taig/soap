package io.taig.android.soap.syntax

import android.content.{ Intent, SharedPreferences }
import io.taig.android.soap._

import scala.language.implicitConversions

trait writer {
    implicit def writerBundleSyntax(
        bundle: Bundle
    ): operation.writer[Bundle] = operation.writer.bundle( bundle )

    implicit def writerIntentSyntax(
        intent: Intent
    ): operation.writer[Intent] = operation.writer.intent( intent )

    implicit def writerSharedPreferencesSyntax(
        sharedPreferences: SharedPreferences
    ): operation.writer[SharedPreferences] = {
        operation.writer.sharedPreferences( sharedPreferences )
    }
}

object writer extends writer