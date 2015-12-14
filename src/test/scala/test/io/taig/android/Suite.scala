package test.io.taig.android

import android.os.Build.VERSION_CODES._
import org.robolectric.annotation.Config
import org.scalatest.{ RobolectricSuite, Matchers, FlatSpec }

@Config( sdk = Array( LOLLIPOP ) )
trait Suite
    extends FlatSpec
    with Matchers
    with RobolectricSuite