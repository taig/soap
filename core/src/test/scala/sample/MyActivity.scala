package sample

import android.app.Activity
import android.content.{ Context, Intent }
import io.taig.android.soap.Bundle
import io.taig.android.soap.implicits._

class MyActivity extends Activity {
    lazy val amount: Option[Int] = getIntent.read[Int]( "amount" )

    var myStateValue: Option[String] = None

    override def onCreate( state: Bundle ): Unit = {
        super.onCreate( state )

        myStateValue = Option( state ).flatMap( _.read[String]( "my-state-value" ) )

        // ...
    }

    // ...

    override def onSaveInstanceState( state: Bundle ): Unit = {
        super.onSaveInstanceState( state )

        state.write( "my-state-value", myStateValue )
    }
}

object MyActivity {
    def apply( amount: Int )( implicit c: Context ): Intent = {
        new Intent( c, classOf[MyActivity] ).write( "amount", amount )
    }
}