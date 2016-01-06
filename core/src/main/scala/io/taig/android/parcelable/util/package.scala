package io.taig.android.parcelable

package object util {
    def printBundle( bundle: Bundle, indent: String = "" ): String = Option( bundle ) match {
        case Some( bundle ) ⇒
            import collection.JavaConversions._

            bundle.keySet().map { key ⇒
                val value = bundle.get( key ) match {
                    case bundle: Bundle ⇒ "\n" + printBundle( bundle, indent + "  " )
                    case value          ⇒ s" $value"
                }

                s"$indent$key:$value"
            }.mkString( "\n" )
        case None ⇒ ""
    }
}