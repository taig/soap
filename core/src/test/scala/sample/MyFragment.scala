package sample

import android.app.Fragment
import io.taig.android.soap.Bundle
import io.taig.android.soap.implicits._
import shapeless._
import shapeless.syntax.singleton._

class MyFragment extends Fragment {
    lazy val coordinates: Option[( Int, Int )] = getArguments.read[( Int, Int )]( "coordinates" )

    lazy val orientation: String = getArguments.read[String]( "orientation" ).get

    override def onCreate( state: Bundle ): Unit = {
        super.onCreate( state )

        val running = Option( state )
            .flatMap( _.read[Boolean]( "running" ) )
            .getOrElse( false )

        // ...
    }

    // ...

    override def onSaveInstanceState( state: Bundle ): Unit = {
        super.onSaveInstanceState( state )

        state.write( "running", true )
    }
}

object MyFragment {
    def apply( coordinates: Option[( Int, Int )] ): MyFragment = {
        val fragment = new MyFragment
        fragment.setArguments(
            Bundle(
                "coordinates" ->> coordinates ::
                    "orientation" ->> "portrait" ::
                    HNil
            )
        )
        fragment
    }
}