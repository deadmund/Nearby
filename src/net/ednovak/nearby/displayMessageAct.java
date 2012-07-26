package net.ednovak.nearby;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;


public class displayMessageAct extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait);
        
        // Instantiate stuff
        protocol p = new protocol();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Intent intent = getIntent();
        
        // Gather variables and call alice (stage 1)
        int method = Integer.valueOf(prefs.getString("poly_method", "2"));
        int pol = intent.getIntExtra("pol", 10);
        int bits = Integer.valueOf(prefs.getString("encryption_strength", "1024"));
        StringBuffer txt = p.alice(1, pol, bits, method); // stage number, policy, enc bits, polynomial method

        // Determine message type and send message
        String rec = intent.getStringExtra("rec");
        String message_type = prefs.getString("message_type", "fb");
        if ( message_type.equals("sms") ){ // sms message
	        // Send the message as a SMS
	    	ArrayList<String> list = new ArrayList<String>();
	    	SmsManager sms = SmsManager.getDefault();
	    	list = sms.divideMessage(String.valueOf(txt));
	    	Log.d("stage 1", "sending the encrypted coefficients (and other stuff) to Bob");
	    	sms.sendMultipartTextMessage(rec, null, list, null, null);
        }
        
        else if ( message_type.equals("fb") ){ // Facebook message
        	p.sendFBMessage(rec, txt.toString(), 1, this); // Recipient, message, stage, context (to bind to service)
        }
        
    } // End of onCreate
    
} // End of activity / class;
    	