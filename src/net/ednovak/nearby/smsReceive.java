package net.ednovak.nearby;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class smsReceive extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent){
		abortBroadcast(); // Stops the broadcast throughout the rest of the OS.
		Bundle bundle = intent.getExtras();
		SmsMessage[] msgs = null;
		String s = "";
		
		if (bundle != null){
			// Get the SMS 
			Object[] pdus = (Object[]) bundle.get("pdus");
			msgs = new SmsMessage[pdus.length];
			for (int i = 0; i < msgs.length; i++){// Goes through multiple messages
				msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
				s = msgs[i].getOriginatingAddress() + ":";
				s += msgs[i].getMessageBody().toString();
			}
		}
		String[] tokens = s.split(":");
		for (int i = 0; i < tokens.length; i++){
			Log.d("receive", "tokens[" + i + "]:" + tokens[i]);
			Toast.makeText(context, tokens[i], Toast.LENGTH_SHORT).show();
		}
		Toast.makeText(context, s, Toast.LENGTH_LONG).show();
		
		if( !tokens[1].equals("@@1") ){
			Log.d("receive", "clearing abort broadcast");
			clearAbortBroadcast();
		}
	}

}
