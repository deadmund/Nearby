package net.ednovak.nearby;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;


public class smsReceive extends BroadcastReceiver {
	
	private LocationManager lManager;
	private final static lListener myListener = new lListener();
	
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
				case 1:
					Log.d("1", "Receiving from Alice!");
					Log.d("receive", s);
					
					// Turn on the listener immediately
			        lManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			        lManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10000, myListener);
			        // Turn on the following for a physical phone, turn it off for emulated device
			        //lManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, lListener);
					
			        // New instance of the protocol
					protocol p = new protocol();
					
					// Convert back to ascii
					for(int i = 2; i < tokens.length; i++){
						tokens[i] = new BigInteger(tokens[i], 16).toString();
					}
					
					Log.d("recieve", "Just checking the hexToAscii tokens[3]: " + tokens[3]);			        

			        
					// Alice's policy width and Bob's location x (longitude)
			        int width = Integer.parseInt( tokens[tokens.length - 3] );
			        //int x = p.longitudeToInt(-179.9991); // just long for now
			        
			        Location lastKnownLocation = lManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			        if ( lastKnownLocation == null ){
			        	lastKnownLocation = lManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			        }
			        if ( lastKnownLocation == null ) {
			        	Log.d("recieve" , "Bob's location is null!");
			        }
			        
			        
			        //double edge = p.findLong(lon, lat, pol);
			        //int edgeLeafNumber = p.longitudeToLeaf(edge);
			        //int aliceLeafNumber = p.longitudeToLeaf(lon);
			        
			        //Log.d("stage 1", "Alice's leaf value: " + aliceLeafNumber);
			        //Log.d("stage 1", "Edge gps lon value: " + edge);
			        //Log.d("stage 1", "edge leaf value: " + edgeLeafNumber);
			        
			        //int spanLength = ( Math.abs(edgeLeafNumber - aliceLeafNumber) * 2 ) + 1;
			        //int left = aliceLeafNumber - (spanLength / 2);
			        //int right = aliceLeafNumber + (spanLength / 2);
			        
			        //Log.d("stage 1", "Alice's leaf nodes go from " + left + " to " + right + ".  Her node is: " + aliceLeafNumber);
			        
			        
			        int bobLeafNumber = p.longitudeToLeaf(lastKnownLocation.getLongitude());			        
			        Log.d("stage 2", "Bob's leaf number: " + bobLeafNumber);
			        
			        int left = bobLeafNumber - width;
			        int right = bobLeafNumber + width;
			        System.out.println("Bob's leaf nodes go from " + left + " to " + right + ".  His node is: " + bobLeafNumber);
			        
			        treeQueue leaves = p.genLeaves(left, right, bobLeafNumber);
			        
			        //System.out.println("Here are the leaves in Bob's span:");
			        //for (int i = 0; i < leaves.length; i++){
			        //	System.out.println("" + leaves.peek(i));
			        //	System.out.println(" ");
			        //}
			        
			        //System.out.println("Building the tree upwards!");
			        tree root = p.buildUp(leaves);
			        System.out.println("The root of Bob's leaves: " + root.toString());
			        
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
			        Paillier paillierE = new Paillier();
			        BigInteger g = new BigInteger(tokens[tokens.length - 2]);
			        BigInteger n = new BigInteger(tokens[tokens.length - 1]);
			        paillierE.loadPublicKey(g, n); 
			        //Log.d("receive", "BOB paillierE.g: " + paillierE.g);
			        //Log.d("receive", "BOB paillierE.n: " + paillierE.n);
			        
			        BigInteger[] results = new BigInteger[(tokens.length -5) * coveringSet.length];
			        Random randomGen = new Random();
			        
			        // This should probs be a protocol function
			        // Evaluate the polys
		        	for (int j = 0; j < coveringSet.length; j++){
		        		int tmp = coveringSet.peek(j).value;
		        		BigInteger bob = new BigInteger(String.valueOf(tmp));
		        		bob = paillierE.Encryption(bob);
		        		for (int i = 2; i < tokens.length - 3; i++){ // The last token is the width
		        			BigInteger alice = new BigInteger(tokens[i]);
		        			BigInteger c = bob.multiply(alice).mod(paillierE.nsquare);
		        			
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
			    		txt += ":" + results[i].toString(16);
			    	}
			    	
			    	Log.d("receive", "the txt we're sending to Alice: " + txt);
			    	
			    	// Dividing the message into txt message size parts 
			    	ArrayList<String> list = new ArrayList<String>();
			    	SmsManager sms = SmsManager.getDefault();
			    	list = sms.divideMessage(txt);
			    	//for(String tmp : list){
			    	//	Log.d("receive", "the items in the list: "+ tmp);
			    	//}	    	
			    	
			    	// Sending a mult-part txt (which solves so much for me!)
			    	String number = tokens[0].substring(7);
			    	//Log.d("receive", "the number: " + number);
			    	sms.sendMultipartTextMessage(number, null, list, null, null);	        
	
			    	break;
			        
				case 2:
					Log.d("2", "Receiving from Bob!");
					Log.d("receive", s);
					
					p = new protocol();
					
					// Convert back to ascii
					for(int i = 2; i < tokens.length; i++){
						tokens[i] = new BigInteger(tokens[i], 16).toString();
					}
					
					Log.d("recieve", "Just checking the hexToAscii tokens[3]: " + tokens[3]);
					
					shareSingleton share = shareSingleton.getInstance();
					Log.d("ALICE", "share.g: " + share.g);
					Log.d("ALICE", "share.lambda: " + share.lambda);
					Log.d("ALICE", "share.n: " + share.n);
					
					Paillier paillierD = new Paillier();
					paillierD.loadPrivateKey(share.g, share.lambda, share.n);
					
					Intent intent2 = new Intent(context, answerAct.class);
					intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent2.putExtra("answer", "Bob is not near you"); //Assume Bob is not near us
					
					for(int i = 2; i < tokens.length; i++){
						BigInteger val = new BigInteger(tokens[i]);
						String clear = paillierD.Decryption(val).toString();
						Log.d("ALICE", "unenc: " + clear);
						if (clear.equals("0")){
							Log.d("hooray!", "It was 0");
							intent2.putExtra("answer", "Bob is located near you!");
							intent2.putExtra("found", true);
						}						
					}
					context.startActivity(intent2);
					
					break;
					
				default:
					Log.d("receive", "clearing abort broadcast because this is not my protocol text number!");
					clearAbortBroadcast();
					break;
			} // End of switch
		} // End of else
	} // End of method
} // End of class
				
				