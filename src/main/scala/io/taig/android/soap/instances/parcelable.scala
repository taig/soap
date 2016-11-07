package io.taig.android.soap.instances

import android.os.{ Parcel, Parcelable }
import io.circe.{ Decoder, Encoder }
import io.circe.syntax._

import scala.reflect._

trait parcelable {
    implicit def soapEncoderParcelable[P <: Parcelable]: Encoder[P] = {
        Encoder.instance[P] { parcelable ⇒
            val parcel = Parcel.obtain()
            parcel.writeParcelable( parcelable, 0 )
            val bytes = parcel.marshall()
            parcel.recycle()
            bytes.asJson
        }
    }

    implicit def soapDecoderParcelable[P <: Parcelable: ClassTag]: Decoder[P] = {
        Decoder[Array[Byte]].map { bytes ⇒
            val parcel = Parcel.obtain()
            parcel.unmarshall( bytes, 0, bytes.length )
            parcel.setDataPosition( 0 )
            parcel.readParcelable[P]( classTag[P].runtimeClass.getClassLoader )
        }
    }
}

object parcelable extends parcelable