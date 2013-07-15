package net.ednovak.nearby;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.util.Random;


public class displayMessageAct extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait); // main reason we even do this
        
        // Here we have Alice asking the initial question, "Bob, where are you?!"
        // We can give this initial session a session number and use that to avoid collision
        
        // Instantiate stuff
        //share.session = session; /not yet implemented....
        protocol p = new protocol();
        Intent intent = getIntent();    
        
        // Send that initial message!
        String rec = intent.getStringExtra("rec");
        
        // Start the protocol
        String session = p.sendFBMessage(rec, "where are you?", this);
        //share.session = session;
        
    } // End of onCreate
    
} // End of activity / class;
    	