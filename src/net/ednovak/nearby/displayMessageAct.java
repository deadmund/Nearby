package net.ednovak.nearby;

import android.app.Activity;
import android.os.Bundle;


@SuppressWarnings("serial")
class longitudeException extends Exception{
	public longitudeException(String msg){
		super(msg);
	}
}

public class displayMessageAct extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait);
        protocol p = new protocol();
        p.alice(1, displayMessageAct.this); // Kicks off the protocol
    } // End of onCreate
    
} // End of activity / class;
    	