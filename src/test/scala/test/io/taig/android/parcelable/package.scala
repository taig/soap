package test.io.taig.android

import java.net.URL

package object parcelable {

    import io.taig.android.parcelable.Parcelable

    @Parcelable
    case class OptionArgument( x: Option[String] )

    @Parcelable
    case class OptionParcelableArgument( x: Option[Primitive] )

    @Parcelable
    case class NestedOptionArgument( x: Option[Option[String]] )

    @Parcelable
    case class PrimitiveArrayArgument( x: Array[Int] ) {
        override def equals( o: scala.Any ) = o match {
            case p: PrimitiveArrayArgument ⇒ x.sameElements( p.x )
            case _                         ⇒ false
        }
    }

    @Parcelable
    case class ParcelableArrayArgument( x: Array[Primitive] ) {
        override def equals( o: scala.Any ) = o match {
            case p: ParcelableArrayArgument ⇒ x.sameElements( p.x )
            case _                          ⇒ false
        }
    }

    @Parcelable
    case class PrimitiveTraversableArguments( x: Seq[Int], y: List[Double] )

    @Parcelable
    case class ParcelableTraversableArgument( x: Seq[Primitive] )

    @Parcelable
    case class PrimitiveMapArgument( x: Map[Int, String] )

    @Parcelable
    case class ParcelableMapArgument( x: Map[Int, Primitive] )

    @Parcelable
    case class PrimitiveTupleArgument( x: ( Int, String, Primitive ) )

    @Parcelable
    case class URLArgument( x: URL )

    @Parcelable
    case class EitherArgument( either: Either[String, ( Int, Primitive )] )
}