package net.ednovak.nearby;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
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
		
		// Splits them up assuming my ':' marker
		String[] tokens = s.split(":");
		
		Log.d("receive", "I just got this: ");
		for (int i = 0; i < tokens.length; i++){
			Log.d("receive", "token:" + tokens[i]);
		}
		
		Log.d("receive", "substring:" + tokens[1].substring(0, 2));
		
		// Stop it if this is not my protocol thus, not a text for me.
		if ( !tokens[1].substring(0, 2).equals("@@") ){
			clearAbortBroadcast();
			Log.d("receive", "clearing abort broadcast because this is not my protocol sms!");
		}
		
		else{
			
			// One toast with the entire thing!
			Toast.makeText(context, s, Toast.LENGTH_LONG).show();	
			
			int swtch = Integer.parseInt(tokens[1].substring(2));
			Log.d("receive", "the code after the @@: " + swtch);
			switch (swtch){
				case 1:	
					// process the encrypted coefficients!
			        
			        // New instance of the protocol
			        protocol p = new protocol();
			        
					// Alice's policy width and Bob's location x (longitude)
			        int width = Integer.parseInt( tokens[tokens.length - 3] );
			        int x = p.longitudeToInt(-179.9991); // just long for now
			        
			        System.out.println("Bob's x: " + x);
			        
			        int left = x - width;
			        int right = x + width;
			        System.out.println("Bob's leaf nodes go from " + left + " to " + right + ".  His node is: " + x);
			        //System.out.println("Lowest possible node on the tree at long=-180: " + this.longitudeToInt(-180.0));
			        //System.out.println("Highest possible node on the tree at long=180: " + this.longitudeToInt(180.0));
			        
			        treeQueue leaves = p.genLeaves(left, right, x);
			        
			        //System.out.println("Here are the leaves in Bob's span:");
			        //for (int i = 0; i < leaves.length; i++){
			        //	System.out.println("" + leaves.peek(i));
			        //	System.out.println(" ");
			        //}
			        
			        //System.out.println("Building the tree upwards!");
			        tree root = p.buildUp(leaves);
			        System.out.println("The root of these leaves is: " + root.toString());
			        
			        //System.out.println("The entire tree:");
			        //System.out.println(p.treeToStringDown(root));
			        
			        System.out.println("Finding Bob's covering set");
			        treeQueue coveringSet = root.findCoverSet(p.user);
			        // Printing Bob's cover set
			        for (int i = 0; i < coveringSet.length; i++){
			        	System.out.println(coveringSet.peek(i).value);
			        	System.out.println("");
			        }
			        
			        // Find the c's
			        Paillier paillier = new Paillier(8, 4);
			        BigInteger g = new BigInteger(tokens[tokens.length - 2]);
			        BigInteger n = new BigInteger(tokens[tokens.length - 1]);
			        paillier.loadPublicKey(g, n);
			        
			        BigInteger[] results = new BigInteger[(tokens.length -5) * coveringSet.length];
			        Random randomGen = new Random();
			        
			        // This should probs be a protocol function
			        // Evaluate the polys
		        	for (int j = 0; j < coveringSet.length; j++){
		        		int tmp = coveringSet.peek(j).value;
		        		BigInteger cur = new BigInteger(String.valueOf(tmp));
		        		cur = paillier.Encryption(cur);
		        		for (int i = 2; i < tokens.length - 3; i++){ // The last token is the width
		        			BigInteger c = cur.multiply(new BigInteger(tokens[i])).mod(paillier.nsquare);
		        			
		        			// Pack them randomly to send back
		        			boolean unplaced = true;
		        			while (unplaced){
		        				int randInt = randomGen.nextInt(results.length);
		        				//Log.d("receive", "trying spot: " + randInt);
		        				if ( results[randInt] == null){
		        					results[randInt] = c;
		        					unplaced = false;
		        				}
		        			}	
		        		}
			        }
			        
		        	// Printing Bob's values
			        Log.d("receive", "Printing Bob Generated values (in random order, encrypted):");
			        for(int i = 0; i < results.length; i++){
			        	Log.d("receive", results[i].toString());
			        }
			    	
			        // Making the string
			    	String txt = "@@2";
			    	for (int i = 0; i < results.length; i++){
			    		txt += ":" + results[i].toString();
			    	}
			    	
			    	Log.d("receive", "the txt we're sending back: " + txt);
			    	
			    	// Dividing the message into txt message size parts 
			    	ArrayList<String> list = new ArrayList<String>();
			    	SmsManager sms = SmsManager.getDefault();
			    	list = sms.divideMessage(txt);
			    	for(String tmp : list){
			    		Log.d("receive", "the items in the list: "+ tmp);
			    	}	    	
			    	
			    	// Sending a mult-part txt (which solves so much for me!)
			    	String number = tokens[0].substring(7);
			    	Log.d("receive", "the number: " + number);
			    	sms.sendMultipartTextMessage(number, null, list, null, null);	        
	
			    	break;
			        
				case 2:
					Log.d("2", "Receiving from Bob!");
					Log.d("receive", s);
					
					break;
					
				default:
					Log.d("receive", "clearing abort broadcast because this is not my protocol text number!");
					clearAbortBroadcast();
					break;
			} // End of switch
		} // End of else
	} // End of method
} // End of class
				
				