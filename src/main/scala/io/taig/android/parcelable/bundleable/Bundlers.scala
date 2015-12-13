package io.taig.android.parcelable.bundleable

private[parcelable] trait Bundlers extends LowPriorityBundlers {
    //    implicit val `Bundler[CNil]` = Bundler[CNil](
    //        _ ⇒ sys.error( "No encode representation of CNil (this shouldn't happen)" ),
    //        _ ⇒ sys.error( "No decode representation of CNil (this shouldn't happen)" )
    //    )
    //
    //    implicit def `Bundler[Either]`[L: Codec.Bundle, R: Codec.Bundle] = Bundler[Either[L, R]](
    //        {
    //            case Left( value )  ⇒ Bundle( "either" ->> -1 :: "value" ->> value :: HNil )
    //            case Right( value ) ⇒ Bundle( "either" ->> 1 :: "value" ->> value :: HNil )
    //        },
    //        bundle ⇒ bundle.read[Int]( "either" ) match {
    //            case -1 ⇒ Left( bundle.read[L]( "value" ) )
    //            case 1  ⇒ Right( bundle.read[R]( "value" ) )
    //        }
    //    )
    //
    //    implicit val `Bundler[HNil]` = Bundler[HNil](
    //        _ ⇒ Bundle.empty,
    //        _ ⇒ HNil
    //    )
    //
    //    implicit def `Bundler[Option]`[T: Codec.Bundle] = Bundler[Option[T]](
    //        {
    //            case Some( value ) ⇒ Bundle( "value", value )
    //            case None          ⇒ Bundle.empty
    //        },
    //        bundle ⇒ bundle.containsKey( "value" ) match {
    //            case true  ⇒ bundle.read[Option[T]]( "value" )
    //            case false ⇒ None
    //        }
    //    )
}

private[parcelable] trait LowPriorityBundlers