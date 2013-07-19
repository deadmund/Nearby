package net.ednovak.nearby;

import android.app.Activity;
import android.content.Intent;
import android.net.TrafficStats;
import android.os.Bundle;
import android.widget.Toast;


public class displayMessageAct extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait); // main reason we even do this
        
        // Here we have Alice asking the initial question, "Bob, where are you?!"
        // We can give this initial session a session number and use that to avoid collision
        
        // Instantiate stuff
        //share.session = session; /not yet implemented....
        shareSingleton share = shareSingleton.getInstance();
        protocol p = new protocol();
        Intent intent = getIntent();    
        
        // Send that initial message!
        String rec = intent.getStringExtra("rec");
        
        share.rxstart = TrafficStats.getTotalRxBytes();
        share.txstart = TrafficStats.getTotalTxBytes();
        
        if (share.rxstart == TrafficStats.UNSUPPORTED || share.txstart == TrafficStats.UNSUPPORTED) {
        	Toast.makeText(this, "Traffic Recording is unsupported", Toast.LENGTH_SHORT).show();
        	share.rxstart = 0;
        	share.txstart = 0;
        }
        
        // Start the protocol
        String session = p.sendFBMessage(rec, "where are you?", this);
        //share.session = session;
        
    } // End of onCreate
    
} // End of activity / class;
    	