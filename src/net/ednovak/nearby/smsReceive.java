package net.ednovak.nearby;

import java.math.BigInteger;
import java.util.ArrayList;

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
					Log.d("stage 2", "Receiving from Alice!");
					Log.d("stage 2", s);
					
					// Turn on the listener immediately
			        lManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			        lManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10000, myListener);
			        // Turn on the following for a physical phone, turn it off for emulated device
			        //lManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, lListener);
					
					
					// Convert back to ascii
					for(int i = 2; i < tokens.length; i++){
						tokens[i] = new BigInteger(tokens[i], 16).toString();
					}
					
					//Log.d("recieve", "Just checking the hexToAscii tokens[3]: " + tokens[3]);			        

			        
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
			        lManager.removeUpdates(myListener);
			        
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
			        
			        // Do Bob's calculations (homomorphic sneaky-ness
			        BigInteger[] results = p.bobCalc(coveringSet, tokens);
			        
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
					
					Intent intent2 = new Intent(context, answerAct.class);
					intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
						intent2.putExtra("answer", "Bob is not located near you!");
						intent2.putExtra("found", found);
						context.startActivity(intent2);
					}
					
					else { // Stage 3 stuff
						Log.d("stage 3", "He's close in long, now to check latitude");
						
						
						share = shareSingleton.getInstance();
						
						double edgeDeg = p.findLat(share.lon, share.lat, share.pol);
						int edgeLeafNumber = p.latitudeToLeaf(edgeDeg);
						int aliceLeafNumber = p.latitudeToLeaf(share.lat);
						
						int spanLength = ( Math.abs(edgeLeafNumber - aliceLeafNumber) * 2 ) + 1;
						left = aliceLeafNumber - (spanLength / 2); // int
						right = aliceLeafNumber + (spanLength / 2); // int
						
						Log.d("stage 3", "Alice's leaf nodes go from " + left + " to " + right + ".  Her node is: " + aliceLeafNumber);
						
						// Making leaves
						leaves = p.genLeaves(left,  right, aliceLeafNumber); // treeQueue
						// Making tree
						root = p.buildUp(leaves); // tree
						Log.d("stage 3", "the root of these leaves is: " + root.toString());
						// Finding rep set
						treeQueue repSet = root.findRepSet(leaves.peek(0), leaves.peek(-1), root);
						// Printing rep set
						Log.d("stage 3", "Finding Alice's latitude rep set");
				        for (int i = 0; i < repSet.length; i++){
				        	Log.d("stage 3", ""+repSet.peek(i).value);
				        }
				        
				        // Generating coefficients (method 1, several polys)
				        int[] coefficients = p.makeCoefficientsOne(repSet);
				        
				        // Encrypting the coefficients
				        // Encrypting Coefficients
						Paillier paillier = new Paillier();
						paillier.loadPublicKey(share.g, share.n);
						BigInteger[] encCoe = new BigInteger[coefficients.length];
						for (int i = 0; i < coefficients.length; i++){
							encCoe[i] = paillier.Encryption(new BigInteger(String.valueOf(coefficients[i])));
						}
						
				        // Generate the message to send to Bob for stage 4
				    	// The format of a stage 4 message:
				    	// "@@4:encrypted coefficients:width:g:n"
				    	txt = "@@3"; //String
				    	for(int i = 0; i < encCoe.length; i++){ // The coefficients encrypted
				    		txt += ":" + encCoe[i].toString(16);
				    	}
				    	// Throwing the key in their too
						txt += ":" + share.pol + ":" + share.g.toString(16) + ":" + share.n.toString(16);
						
						// Send txts
				    	list = new ArrayList<String>(); //ArrayList<String>
				    	sms = SmsManager.getDefault(); // SmsManager
				    	list = sms.divideMessage(txt);
				    	
				    	sms.sendMultipartTextMessage(share.number, null, list, null, null);

					}
					
					break;
					
				case 3: // Stage 4 (bob does his part, latitude)
					Log.d("stage 4", "Recieving latitude from Alice! (our longitude was probs close");
					Log.d("recieve", s);
					
					// Turn on the listener immediately
			        lManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			        lManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10000, myListener);
			        // Turn on the following for a physical phone, turn it off for emulated device
			        //lManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, lListener);
					
					// Convert back to ascii
					for(int i = 2; i < tokens.length; i++){
						tokens[i] = new BigInteger(tokens[i], 16).toString();
					}		        

			        
					// Alice's policy width and Bob's location x (latitude)
			        width = Integer.parseInt( tokens[tokens.length - 3] ); // int
			        //int x = p.longitudeToInt(-179.9991); // just long for now
			        
			        
			        lastKnownLocation = lManager.getLastKnownLocation(LocationManager.GPS_PROVIDER); // Location
			        if ( lastKnownLocation == null ){
			        	lastKnownLocation = lManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			        }
			        if ( lastKnownLocation == null ) {
			        	Log.d("Stage 4" , "Bob's location is null!");
			        }
			        lManager.removeUpdates(myListener);
					
			        bobLeafNumber = p.latitudeToLeaf(lastKnownLocation.getLatitude()); // int	        
			        Log.d("stage 4", "Bob's leaf number: " + bobLeafNumber);
			        
			        left = bobLeafNumber - width; //int
			        right = bobLeafNumber + width; //int
			        Log.d("stage 4", "Bob's leaf nodes go from " + left + " to " + right + ".  His node is: " + bobLeafNumber);
			        
			        leaves = p.genLeaves(left, right, bobLeafNumber); // treeQueue
			        root = p.buildUp(leaves); // tree
			        Log.d("stage 4", "the root of Bob's leaves: " + root);
			        
			        coveringSet = root.findCoverSet(p.user);
			        // Printing Bob's cover set
			        for (int i = 0; i < coveringSet.length; i++){
			        	Log.d("stage 4", "covering set: " +coveringSet.peek(i).value);
			        }
			        
			        results = p.bobCalc(coveringSet, tokens); // BigInteger[]
			        
			        // Making the string
			    	txt = "@@4"; //String
			    	for (int i = 0; i < results.length; i++){
			    		txt += ":" + results[i].toString(16);
			    	}
			    	
			    	Log.d("stage 4", "the txt we're sending to Alice: " + txt);
			    	
			    	// Dividing the message into txt message size parts 
			    	list = new ArrayList<String>(); // ArrayList<String>
			    	sms = SmsManager.getDefault(); // SmsManager
			    	list = sms.divideMessage(txt);
			    	//for(String tmp : list){
			    	//	Log.d("receive", "the items in the list: "+ tmp);
			    	//}	    	
			    	
			    	// Sending a mult-part txt (which solves so much for me!)
			    	number = tokens[0].substring(7); // number
			    	//Log.d("receive", "the number: " + number);
			    	sms.sendMultipartTextMessage(number, null, list, null, null);	    
					
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
				
				
