package io.taig.android.soap.instances

import android.annotation.TargetApi
import android.util.{ Size, SizeF }
import io.circe.{ Decoder, Encoder }

@TargetApi( 21 )
trait size {
    implicit val soapEncoderSize: Encoder[Size] = {
        Encoder[( Int, Int )].contramap { size ⇒
            ( size.getWidth, size.getHeight )
        }
    }

    implicit val soapDecoderSize: Decoder[Size] = {
        Decoder[( Int, Int )].map {
            case ( width, height ) ⇒ new Size( width, height )
        }
    }

    implicit val soapEncoderSizeF: Encoder[SizeF] = {
        Encoder[( Float, Float )].contramap { size ⇒
            ( size.getWidth, size.getHeight )
        }
    }

    implicit val soapDecoderSizeF: Decoder[SizeF] = {
        Decoder[( Float, Float )].map {
            case ( width, height ) ⇒ new SizeF( width, height )
        }
    }
}

object size extends size