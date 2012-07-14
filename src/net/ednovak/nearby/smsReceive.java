package net.ednovak.nearby;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class smsReceive extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent){
		Bundle bundle = intent.getExtras();
		SmsMessage[] msgs = null;
		String s = "";
		
		if (bundle != null){
			// Get the SMS 
			Object[] pdus = (Object[]) bundle.get("pdus");
			msgs = new SmsMessage[pdus.length];
			for (int i = 0; i < msgs.length; i++){
				msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
				s = "SMS from " + msgs[i].getOriginatingAddress();
				s += " :";
				s += msgs[i].getMessageBody().toString();
				s += "\n";
			}
		}
		Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
	}

}
