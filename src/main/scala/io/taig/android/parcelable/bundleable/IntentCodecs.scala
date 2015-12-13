package io.taig.android.parcelable.bundleable

import scala.language.higherKinds

private[parcelable] trait IntentCodecs extends LowPriorityIntentCodecs {
    //    implicit val `Codec.Intent[Array[Boolean]]` = Codec.Intent[Array[Boolean]](
    //        _.putExtra( _, _ ),
    //        _.getBooleanArrayExtra( _ )
    //    )
    //
    //    implicit val `Codec.Intent[Array[Byte]]` = Codec.Intent[Array[Byte]](
    //        _.putExtra( _, _ ),
    //        _.getByteArrayExtra( _ )
    //    )
    //
    //    implicit val `Codec.Intent[Array[Char]]` = Codec.Intent[Array[Char]](
    //        _.putExtra( _, _ ),
    //        _.getCharArrayExtra( _ )
    //    )
    //
    //    implicit val `Codec.Intent[Array[CharSequence]]` = new Codec.Intent[Array[CharSequence]] {
    //        @TargetApi( 8 )
    //        override def encode( intent: Intent, key: String, value: Array[CharSequence] ) = intent.putExtra( key, value )
    //
    //        @TargetApi( 8 )
    //        override def decode( intent: Intent, key: String ) = intent.getCharSequenceArrayExtra( key )
    //    }
    //
    //    implicit val `Codec.Intent[Array[Double]]` = Codec.Intent[Array[Double]](
    //        _.putExtra( _, _ ),
    //        _.getDoubleArrayExtra( _ )
    //    )
    //
    //    implicit val `Codec.Intent[Array[Float]]` = Codec.Intent[Array[Float]](
    //        _.putExtra( _, _ ),
    //        _.getFloatArrayExtra( _ )
    //    )
    //
    //    implicit val `Codec.Intent[Array[Int]]` = Codec.Intent[Array[Int]](
    //        _.putExtra( _, _ ),
    //        _.getIntArrayExtra( _ )
    //    )
    //
    //    implicit val `Codec.Intent[Array[Long]]` = Codec.Intent[Array[Long]](
    //        _.putExtra( _, _ ),
    //        _.getLongArrayExtra( _ )
    //    )
    //
    //    implicit val `Codec.Intent[Array[Parcelable]]` = Codec.Intent[Array[Parcelable]](
    //        _.putExtra( _, _ ),
    //        _.getParcelableArrayExtra( _ )
    //    )
    //
    //    implicit val `Codec.Intent[Array[Short]]` = Codec.Intent[Array[Short]](
    //        _.putExtra( _, _ ),
    //        _.getShortArrayExtra( _ )
    //    )
    //
    //    implicit val `Codec.Intent[Array[String]]` = Codec.Intent[Array[String]](
    //        _.putExtra( _, _ ),
    //        _.getStringArrayExtra( _ )
    //    )
    //
    //    implicit val `Codec.Intent[Boolean]` = Codec.Intent[Boolean](
    //        _.putExtra( _, _ ),
    //        _.getBooleanExtra( _, false )
    //    )
    //
    //    implicit val `Codec.Intent[Bundle]` = Codec.Intent[Bundle](
    //        _.putExtra( _, _ ),
    //        _.getBundleExtra( _ )
    //    )
    //
    //    implicit val `Codec.Intent[Byte]` = Codec.Intent[Byte](
    //        _.putExtra( _, _ ),
    //        _.getByteExtra( _, Byte.MinValue )
    //    )
    //
    //    implicit val `Codec.Intent[Char]` = Codec.Intent[Char](
    //        _.putExtra( _, _ ),
    //        _.getCharExtra( _, Char.MinValue )
    //    )
    //
    //    implicit val `Codec.Intent[CharSequence]` = Codec.Intent[CharSequence](
    //        _.putExtra( _, _ ),
    //        _.getCharSequenceExtra( _ )
    //    )
    //
    //    implicit val `Codec.Intent[Double]` = Codec.Intent[Double](
    //        _.putExtra( _, _ ),
    //        _.getDoubleExtra( _, Double.MinValue )
    //    )
    //
    //    implicit val `Codec.Intent[Float]` = Codec.Intent[Float](
    //        _.putExtra( _, _ ),
    //        _.getFloatExtra( _, Float.MinValue )
    //    )
    //
    //    implicit val `Codec.Intent[Int]` = Codec.Intent[Int](
    //        _.putExtra( _, _ ),
    //        _.getIntExtra( _, Int.MinValue )
    //    )
    //
    //    implicit val `Codec.Intent[Long]` = Codec.Intent[Long](
    //        _.putExtra( _, _ ),
    //        _.getLongExtra( _, Long.MinValue )
    //    )
    //
    //    implicit def `Codec.Intent[Parcelable]`[T <: Parcelable] = Codec.Intent[T](
    //        _.putExtra( _, _ ),
    //        _.getParcelableExtra[T]( _ )
    //    )
    //
    //    implicit val `Codec.Intent[Short]` = Codec.Intent[Short](
    //        _.putExtra( _, _ ),
    //        _.getShortExtra( _, Short.MinValue )
    //    )
    //
    //    implicit val `Codec.Intent[String]` = Codec.Intent[String](
    //        _.putExtra( _, _ ),
    //        _.getStringExtra( _ )
    //    )
    //
    //    implicit def `Codec.Intent[Traversable]`[A: ClassTag, T[X] <: Traversable[X]](
    //        implicit
    //        c:   Codec.Intent[Array[A]],
    //        cbf: CanBuildFrom[Nothing, A, T[A]]
    //    ) = {
    //        Codec.Intent[T[A]](
    //            ( intent, key, value ) ⇒ c.encode( intent, key, value.toArray ),
    //            ( intent, key ) ⇒ c.decode( intent, key ).to[T]
    //        )
    //    }
}

private[parcelable] trait LowPriorityIntentCodecs {
    //    implicit def `Codec.Intent[Codec.Bundle]`[T: Codec.Bundle] = Codec.Intent[T](
    //        ( intent, key, value ) ⇒ intent.write( key, Bundle( "value", value ) ),
    //        _.read[Bundle]( _ ).read[T]( "value" )
    //    )
}