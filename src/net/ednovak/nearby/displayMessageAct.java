package net.ednovak.nearby;

import android.app.Activity;
import android.os.Bundle;


public class displayMessageAct extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait);
        protocol p = new protocol();
        p.alice(1, getApplicationContext()); // Kicks off the protocol
    } // End of onCreate
    
} // End of activity / class;
    	