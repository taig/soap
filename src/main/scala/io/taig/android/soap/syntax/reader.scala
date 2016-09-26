package io.taig.android.soap.syntax

import android.content.{ Intent, SharedPreferences }
import io.taig.android.soap._

import scala.language.implicitConversions

trait reader {
    implicit def readerBundleSyntax(
        bundle: Bundle
    ): operation.reader[Bundle] = operation.reader.bundle( bundle )

    implicit def readerIntentSyntax(
        intent: Intent
    ): operation.reader[Intent] = operation.reader.intent( intent )

    implicit def readerSharedPreferencesSyntax(
        sharedPreferences: SharedPreferences
    ): operation.reader[SharedPreferences] = {
        operation.reader.sharedPreferences( sharedPreferences )
    }
}

object reader extends reader