package sample

import android.app.Fragment
import cats.Eval
import io.taig.android.soap.Bundle
import io.taig.android.soap.implicits._

class MyFragment extends Fragment {
    val coordinates: Eval[Option[( Int, Int )]] = Eval.later {
        getArguments.read[( Int, Int )]( "coordinates" )
    }

    val orientation: Eval[String] = Eval.later {
        getArguments.read[String]( "orientation" ).get
    }

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
            Bundle( 2 )
                .write( "coordinates", coordinates )
                .write( "orientation", "portrait" )
        )
        fragment
    }
}