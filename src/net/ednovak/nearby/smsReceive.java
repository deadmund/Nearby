package net.ednovak.nearby;

import java.math.BigInteger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;


public class smsReceive extends BroadcastReceiver {
	
	private final static lListener myListener = new lListener();
	
	@Override
	public void onReceive(Context context, Intent intent){
		abortBroadcast(); // Stops the broadcast throughout the rest of the OS.
		Bundle bundle = intent.getExtras();
		SmsMessage[] msgs = null;
		String s = "";
		protocol p = new protocol();
		
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
		
		// Splits them up assuming my ':' marker
		String[] tokens = s.split(":");
		
		Log.d("receive", "just got this: ");
		for (int i = 0; i < tokens.length; i++){
			Log.d("receive", "token:" + tokens[i]);
		}
		
		//Log.d("receive", "substring:" + tokens[1].substring(0, 2));
		
		// Stop it if this is not my protocol thus, not a text for me.
		if ( !tokens[1].substring(0, 2).equals("@@") ){
			clearAbortBroadcast();
			Log.d("receive", "clearing abort broadcast because this is not my protocol sms!");
		}
		
		else{
			
			// One toast with the entire thing!
			Toast.makeText(context, s, Toast.LENGTH_LONG).show();	
			
			int swtch = Integer.parseInt(tokens[1].substring(2));
			//Log.d("BOB", "the code after the @@: " + swtch);
			switch (swtch){
				case 1: // stage 2 (Bob does his part for Alice, longitude)
				    p.Bob(2, s, tokens, myListener, context);
			    	break;
			        
				case 2: //stage 3 (alice finds latitude if she's near Bob in longitude)
					Log.d("stage 3", "Receiving from Bob! Check his long and generate lat");
					Log.d("stage 3", s);
					
					
					// Convert back to ascii
					for(int i = 2; i < tokens.length; i++){
						tokens[i] = new BigInteger(tokens[i], 16).toString();
					}
					
					//Log.d("recieve", "Just checking the hexToAscii tokens[3]: " + tokens[3]);
					
					
					//Log.d("ALICE", "share.g: " + share.g);
					//Log.d("ALICE", "share.lambda: " + share.lambda);
					//Log.d("ALICE", "share.n: " + share.n);
					
					Paillier paillierD = new Paillier();
					shareSingleton share = shareSingleton.getInstance();
					paillierD.loadPrivateKey(share.g, share.lambda, share.n);
					
					// Check the latitude and see if we need to continue
					boolean found = false;
					for(int i = 2; i < tokens.length; i++){
						BigInteger val = new BigInteger(tokens[i]);
						String clear = paillierD.Decryption(val).toString();
						Log.d("ALICE", "unenc: " + clear);
						if (clear.equals("0")){
							Log.d("hooray!", "It was 0");
							found = true;
						}						
					}
					
					if ( !found ){
						Intent intent2 = new Intent(context, answerAct.class);
						intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent2.putExtra("answer", "Bob is not located near you!");
						intent2.putExtra("found", found);
						context.startActivity(intent2);
					}
					
					else { // Stage 3 stuff
						
						p.alice(3, context);
					}
					
					break;
					
				case 3: // Stage 4 (bob does his part, latitude)
					Log.d("stage 4", "Recieving latitude from Alice! (our longitude was probs close");
					Log.d("recieve", s);
					
					p.Bob(4, s, tokens, myListener, context);
					 
					break;
					
				case 4: // Stage 5 (Alice sees if she's near Bob in latitude as well! 
					Log.d("stage 5", "Alice is checking bob's latitude C's"); // just like stage 3
					Log.d("receive", s);
					
					// Convert back to ascii
					for(int i = 2; i < tokens.length; i++){
						tokens[i] = new BigInteger(tokens[i], 16).toString();
					}
					
					paillierD = new Paillier();
					share = shareSingleton.getInstance();
					paillierD.loadPrivateKey(share.g, share.lambda, share.n);
					
					Intent intent3 = new Intent(context, answerAct.class);
					intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					found = false; // boolean
					
					for(int i = 2; i < tokens.length; i++){
						BigInteger val = new BigInteger(tokens[i]);
						String clear = paillierD.Decryption(val).toString();
						Log.d("ALICE", "unenc: " + clear);
						if (clear.equals("0")){
							Log.d("hooray!", "It was 0");
							found = true;
						}						
					}
					
					if ( found ) {
						intent3.putExtra("answer", "Bob is near you!");
					}
					
					else {
						intent3.putExtra("answer", "Bob is not near you");
					}
					intent3.putExtra("found", found);
					context.startActivity(intent3);
					break;
					
					
				default:
					Log.d("receive", "clearing abort broadcast because this is not my protocol text number!");
					clearAbortBroadcast();
					break;
			} // End of switch
		} // End of else
	} // End of method
} // End of class
				
				
