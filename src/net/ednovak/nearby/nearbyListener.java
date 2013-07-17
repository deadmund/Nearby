package net.ednovak.nearby;

import java.math.BigInteger;
import java.util.ArrayList;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

public class nearbyListener implements MessageListener {
	
	private Context context;
	private ArrayList<buffer> buffs = new ArrayList<buffer>();
	
	public nearbyListener(Context nContext){
		context = nContext;
		//buffs = nBuffs;
	}
	
	@Override
	public void processMessage(Chat chat, Message message) {
		//Log.d("xmpp", "Chat recieved in thread: " + message.getBody());
		Log.d("xmpp", "Chat recieved in thread");
		//Log.d("xmpp", "message: " + message.getBody().toString());
		
		buffer buff = parseIncoming(message);
		
		// Message stream over, time to process this buffer
		if ( buff != null ) {
			
			//Log.d("receive", "buff.message: " + buff.message);
			
			// Initialize stuff
			protocol p = new protocol();
			//shareSingleton share = shareSingleton.getInstance();
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			
			long end = System.currentTimeMillis();
			long total_recTime = end - buff.start;
			//Log.d("stats", "It took: " + total_recTime + "ms to recieve all chunks");
			
			String sender = buff.sender;
			String[] parts = buff.message.split(":");
			int stage = Integer.valueOf(parts[0]);
			shareSingleton share = shareSingleton.getInstance();
			long checkpoint;
			
			switch (stage){	
				case 1: // The Polynomial (Case 1, stage 1), Bob receiving query from Alice
					if (parts[2].equals("where are you?")) {
						share.start = System.currentTimeMillis(); // This is on Bob
						Log.d("c", "Recieved query from Alice");
						// Bob only wants to share if Alice is near him so lets run the protocol
						
						// Initialize
						int bits = Integer.valueOf(prefs.getString("bits", "1024"));
						int policy = Integer.valueOf(prefs.getString("policy", "160000"));
						int method = Integer.valueOf(prefs.getString("poly_method", "1"));
						//Log.d("test", "method: " + method + "  policy: " + policy + "  bits: " + bits);
						//share = shareSingleton.getInstance();
						//share.pKey = null; // Make sure it's null at beginning of protocol
						
						
						long findWallLon = System.currentTimeMillis();
						//Log.d("Bob checkpoint", "Starting finding wall set "+ (findWallLon - share.start));
						// Find span
						int[] span = new int[3];
						span = p.makeSpan(2, p.locSimple(context), policy);
						
						// Generate leaves
						treeQueue leaves = p.genLeaves(span[0], span[2], span[1], "lon");
						//Log.d("bob-stats", "The span size: " + leaves.length);
						
						// Generate root
						tree root = p.buildUp(leaves);
						//Log.d("bob-stats", "The tree has " + root.count() + " nodes");
						
						// Find Wall
						treeQueue wall = p.findWall(leaves.peek(0), leaves.peek(-1), root);
						long findWallLonEnd = System.currentTimeMillis();
						Log.d("nearbyListener:case1", "Wall set length: " + wall.length);
						//log.d("b checkpoint", "Finding wall set finished " + (findWallLonEnd - share.start));
						//log.d("b checkpoint", "Time to find wall set " + (findWallLonEnd - findWallLon));
						
						
						// Find Coefficients
						long findCoefficientsLon = System.currentTimeMillis();
						//log.d("b checkpoint", "Finding coefficients " + (findCoefficientsLon - share.start));
						BigInteger[] coefficients = p.makeCoefficients(wall, method);
						long findCoefficientsLonEnd = System.currentTimeMillis();
						//log.d("b checkpoint", "Finished finding coefficients " + (findCoefficientsLonEnd - share.start));
						//log.d("stats-bob", "Time to find coefficients " + (findCoefficientsLonEnd - findCoefficientsLon));
						//log.d("bob-stats", "Number of Coefficients: " + coefficients.length);
						
						
						// Encrypt Coefficients
						// Make sure the key is cleared:
						share = shareSingleton.getInstance();
						share.pKey = null;
						BigInteger[] encCoe = p.encryptArray(coefficients, context);
						//Log.d("test", "Bob's encrypted Coe's");
						
						//log.d("curious", "The largest radix allowed: " + Character.MAX_RADIX);
						
						// Put them in a string for sending
						StringBuffer txt = new StringBuffer();
						//Log.d("c", "EncCoe[0]: " + encCoe[0]);
						//Log.d("c", "EncCoe[0] base 32: " + encCoe[0].toString(32));
						txt.append(encCoe[0].toString(32)); // The first one should not begin with ":"
						txt.append(":"+wall.peek(0).height);
						for (int i = 1; i < encCoe.length; i++){
							txt.append(":" + encCoe[i].toString(32));
							txt.append(":" + wall.peek(i).height);
						}
						
						// Extra Stuff
						BigInteger[] key = p.getKey(1024).publicKey();
						txt.append(":" + policy);
						txt.append(":" + bits);
						txt.append(":" + key[0].toString(32)); // g
						txt.append(":" + key[1].toString(32)); // n
						txt.append(":" + method); // Poly method used
						// Other party needs these values						

						//Log.d("test", "Message: " + txt.toString());
						
						// Send to Alice
						p.sendFBMessage(sender, txt.toString(), 3, buff.session, context);
					}
					break;
				// End of case 1
			
				// This is Alice now, recieving the longitude wall from Bob and doing her computation
				case 3: // The computation (case 3, stage 3)
					// Initialize
					//log.d("a checkpoint", "Alice has received longitude coefficients "+ (System.currentTimeMillis() - share.start));
					Log.d("c", "Recieved Coefficents from Bob");

					int policy = Integer.valueOf( parts[parts.length - 5] );
					int bits = Integer.valueOf( parts[parts.length - 4] ); //encryption strength
					BigInteger g = new BigInteger( parts[parts.length - 3], 32 );
					BigInteger n = new BigInteger( parts[parts.length - 2], 32 );
					int method = Integer.valueOf( parts[parts.length - 1]);
					
					
					// Make Span
					//int[] span = new int[3];
					int[] span = p.makeSpan(3, p.locSimple(context), 160934); // 160.934km = 100mi
					
					// Generate Leaves
					//treeQueue leaves = p.genLeaves(span[0], span[2], span[1]);
					
					// Generate Alice's leaf.
					String mapString = new StringBuffer(Integer.toBinaryString(span[1])).toString();
					tree alice = new tree(span[1], mapString.toCharArray(), null, null, 0, "lon");
					//log.d("stats-alice", "alice value: " + alice.value);
					
					// Find Path
					long pathStart = System.currentTimeMillis();
					treeQueue path = p.findPath(alice, p.getPathLength(policy)); // User's location leaf node
					long pathEnd = System.currentTimeMillis();
					//log.d("stats-alice", "Time to find path: " + (pathEnd - pathStart));
					//log.d("stats-alice", "User's path length: " + path.length);
					
					// Pull out Encrypted Coefficients
					//for(int i = 0; i < parts.length; i++){
					//	Log.d("test", "parts[" + i + "]: " + parts[i]);
					//}
					
					// I don't know how many coefficients were sent.  I only know
					// how many things were sent, and how many non-coefficients are sent
					// parts.length - 7 will always be even because the coefficients and heights come in pairs
					BigInteger[] encCoe = new BigInteger[(parts.length - 7) / 2];
					//log.d("nearbyListener:case3", "encCoe length: " + encCoe.length);
					int[] heights = new int[encCoe.length];
					for(int i = 0; i < encCoe.length; i++){
						encCoe[i] = new BigInteger(parts[(i*2)+2], 32);
						//log.d("nearbyListener:case3", "encCoe[i]: " + encCoe[i]);
						heights[i] = Integer.valueOf(parts[(i*2)+2+1]);
						//log.d("nearbyListener:case3", "heights[i]: " + heights[i]);
						//Log.d("test", "encCoe[" + i + "]:" + encCoe[i]);
					}
					
					// Do the actual homomorphic computation
					//Log.d("test", "Homomorphic computation time");
					long compStart = System.currentTimeMillis();
					//log.d("checkpoint", "Starting homomorphic computation "+ (System.currentTimeMillis() - share.start));
					ArrayList<BigInteger> results = p.computation(path, encCoe, heights, bits, g, n, method);
					long compEnd = System.currentTimeMillis();
					//log.d("checkpoint", "Homomorphic computation finished "+ (System.currentTimeMillis() - share.start));
					//log.d("stats-alice", "Time to perform homomorphic encryption: " + (compEnd - compStart));
					//log.d("stats-alice", "Number of results generated: " + results.size());
					
					// Put them in a string for sending
					StringBuffer txt = new StringBuffer();
					txt.append(results.get(0).toString(32));
					for (int i = 1; i < results.size(); i++){
						txt.append(":" + results.get(i).toString(32));
					}
					
					// Send that shit dawg
					p.sendFBMessage(sender, txt.toString(), 4, buff.session, context);
					break;
				// End of stage 3 (case 3)
					
				case 4: // The Check (stage 4, case 4)
					//log.d("test", "checking!");
					Log.d("c", "Checking Alice's Evaluations");
					
					// Pull out the C values
					String[] cValues = new String[parts.length - 2];
					for(int i = 0; i < cValues.length; i++){
						cValues[i] = parts[i+2];
					}
					
					// Check the C values
					bits = Integer.valueOf(prefs.getString("bits", "1024"));
					share = shareSingleton.getInstance();
					
					long checkLonStart = System.currentTimeMillis();
					//log.d("checkpoint", "Starting to check values " + (checkLonStart - share.start));
					share.foundLon = p.check(cValues, context, bits);
					long checkLonEnd = System.currentTimeMillis();
					//log.d("checkpoint", "Finished Checking " + (checkLonEnd - share.start));
					//log.d("checkpoint", "time to check " + (checkLonEnd - checkLonStart));
					//log.d("output", "Same location: " + share.foundLon);
					
					// not sure why bits and method give the 'already instantiated' error but policy does not.
					// Also, if the uses changes their preferences (mid run) there is a problem
					// BEGIN THE LATITUDE ROUND (stage 5)
					bits = Integer.valueOf(prefs.getString("bits", "1024"));
					policy = Integer.valueOf(prefs.getString("policy", "160000"));
					method = Integer.valueOf(prefs.getString("poly_method", "1"));
					
					long findWallLatStart = System.currentTimeMillis();
					//log.d("checkpoint", "Starting finding the wall set latitude " + (findWallLatStart - share.start));
					// Find Span
					span = p.makeSpan(5, p.locSimple(context), policy);
					
					// Generate Leaves
					treeQueue leaves = p.genLeaves(span[0], span[2], span[1], "lat");
					
					// Check Bob's Location
					//log.d("checking", "Bob's latitude value: " + span[1]);
					
					// Find Root
					tree root = p.buildUp(leaves);
					
					// Find Wall
					treeQueue wall = p.findWall(leaves.peek(0), leaves.peek(-1), root);
					long findWallLatEnd = System.currentTimeMillis();
					//log.d("checkpoint", "Finding lat wall set finished " + (findWallLatEnd - share.start));
					//log.d("stats-bob", "time to find wall set lat: " + (findWallLatEnd - findWallLatStart));
					//Log.d("wall", "The leaves in the  wall");
					//for(int i = 0; i < wall.length; i++){
					//	Log.d("wall", "" + wall.peek(i).value);
					//}
					
					// Find Coefficients (latitude now)
					BigInteger[] coefficients = p.makeCoefficients(wall, method);
					//log.d("checkpoint", "Done finding lat coefficients " + (System.currentTimeMillis() - share.start));					
					
					// Encrypt Coefficients
					encCoe = p.encryptArray(coefficients, context);
					
					// Put them in a string for sending
					txt = new StringBuffer();
					txt.append(encCoe[0].toString(32));
					txt.append(":"+wall.peek(0).height);
					for (int i = 1; i < encCoe.length; i++){
						txt.append(":" + encCoe[i].toString(32));
						txt.append(":" + wall.peek(i).height);
					}
					
					// Extra Stuff
					BigInteger[] key = p.getKey(1024).publicKey();
					txt.append(":" + policy);
					txt.append(":" + bits);
					txt.append(":" + key[0].toString(32)); // g
					txt.append(":" + key[1].toString(32)); // n
					txt.append(":" + method); // Poly method used
					// Other party needs these values
					
					// Send To Alice (latitude)
					p.sendFBMessage(sender, txt.toString(), 6, buff.session, context);
					break;
				// End of stage 5 (case 4)
				
				// Alice does the latitude computation
				case 6: //(stage 6, case 6)
					// Initialize
					//log.d("checkpoint", "Alice has received latitude coefficients " + (System.currentTimeMillis() - share.start));
					Log.d("c", "Recieved latitude coefficients");
					policy = Integer.valueOf( parts[parts.length - 5] );
					bits = Integer.valueOf( parts[parts.length - 4] );
					g = new BigInteger( parts[parts.length - 3], 32 );
					n = new BigInteger( parts[parts.length - 2], 32 );
					method = Integer.valueOf( parts[parts.length - 1]);
					
					// Make Span
					//int[] span = new int[3];
					span = p.makeSpan(6, p.locSimple(context), 160934); // 160.934km = 100mi
					
					// Generate Leaves
					//treeQueue leaves = p.genLeaves(span[0], span[2], span[1]);
					
					// Generate Alice's latitude leaf.
					mapString = new StringBuffer(Integer.toBinaryString(span[1])).toString();
					alice = new tree(span[1], mapString.toCharArray(), null, null, 0, "lat");
					//log.d("checking", "alice: " + alice.value);
					
					// Find Path
					path = p.findPath(alice, p.getPathLength(policy)); // User's location leaf node
					//log.d("stats", "Alice's latitude path length: " + path.length);
					//for(int i = 0; i < path.length; i++){
					//	Log.d("path", "" + path.peek(i).value);
					//}
					
					// Pull out Encrypted Coefficients
					//for(int i = 0; i < parts.length; i++){
					//	Log.d("test", "parts[" + i + "]: " + parts[i]);
					//}
					encCoe = new BigInteger[(parts.length - 7) / 2];
					heights = new int[encCoe.length];
					for(int i = 0; i < encCoe.length; i++){
						encCoe[i] = new BigInteger(parts[(i*2)+2], 32);
						heights[i] = Integer.valueOf(parts[(i*2)+2+1]);
						//Log.d("test", "encCoe[" + i + "]:" + encCoe[i]);
					}
					
					
					// Do the actual homomorphic computation
					//log.d("test", "Homomorphic computation time");
					long compStartLat = System.currentTimeMillis();
					//log.d("checkpoint", "Starting homomorphic computation latitude "+ (System.currentTimeMillis() - share.start));
					results = p.computation(path, encCoe, heights, bits, g, n, method);
					long compEndLat = System.currentTimeMillis();
					//log.d("checkpoint", "Homomorphic computation latitude finished "+ (System.currentTimeMillis() - share.start));
					//log.d("stats-alice", "Time to perform homomorphic encryption: " + (compEndLat - compStartLat));
					//log.d("stats-alice", "Number of results generated: " + results.size());
					
					
					// Put them in a string for sending
					txt = new StringBuffer();
					txt.append(results.get(0).toString(32));
					for (int i = 1; i < results.size(); i++){
						txt.append(":" + results.get(i).toString(32));
					}
					
					// Generate a public / private pair for Bob to use
					share = shareSingleton.getInstance();
					share.pKey = null; // Throw away the protocol key
					
					Paillier last = p.getKey(1024); // Generate the coordinates key
					
					BigInteger[] pub = last.publicKey();
					txt.append(":" + pub[0].toString(32)); // g
					txt.append(":" + pub[1].toString(32)); // n
					
					// Send that shit dawg
					p.sendFBMessage(sender, txt.toString(), 7, buff.session, context);
					break;
				// End of stage 6 (case 6)
					
				case 7: // Bob checks (case 7, stage 7) final stage
					Log.d("c", "Checking Alice's latitude evaluations");
					
					// Pull out the C values
					cValues = new String[parts.length - 4];
					for(int i = 0; i < cValues.length; i++){
						cValues[i] = parts[i+2];
					}
					
					// Check the C values
					bits = Integer.valueOf(prefs.getString("bits", "1024"));
					long checkLatStart = System.currentTimeMillis();
					//log.d("checkpoint", "Starting to check the lat values: " + (checkLatStart - share.start));
					boolean latResult = p.check(cValues, context, bits);
					//log.d("output", "Same location: " + latResult);
					long checkLatEnd = System.currentTimeMillis();
					//log.d("checkpoint", "Finished checking lat values " + (checkLatEnd - share.start));
					//log.d("bob-stats", "Time to check values " + (checkLatEnd - checkLatStart));
					long tmpBegin = System.currentTimeMillis();
					
					share.pKey = null; // Throw away the key.  This query is done with it
					
					// Determine if near!
					boolean near = latResult && share.foundLon;
					
					Log.d("checkpoint", "We know if Alice is near, time passed : " + (System.currentTimeMillis() - share.start));

					// Notify Bob (myself)
					String contentTitle = sender + " queried you";
					String contentText;
					Location l = p.locSimple(context);
					
					if (near){
						contentText = "Your location was shared";
					}
					else{
						contentText = "Your location was not shared";
						l.setLatitude(0.0);
						l.setLongitude(0.0);
					}
					
					Intent intent = new Intent(context, MainActivity.class);
					p.notification("Nearby Query Processed", contentTitle, contentText, context, intent);
					
					// Encrypt location
					g = new BigInteger(parts[parts.length - 2], 32);
					n = new BigInteger(parts[parts.length - 1], 32);
					//last = p.getKey(1024);
					last = new Paillier(false, bits, 64);
					last.loadPublicKey(g, n);
					
					long tmpEnd = System.currentTimeMillis();
					double[] orig = {l.getLatitude(), l.getLongitude()};
					String[] sendingLocation = new String[2];
					for (int i = 0; i < orig.length; i++){
						orig[i] = orig[i]*10000; // Multiply by 100 to capture some decimals
						orig[i] = Math.abs(orig[i]); // Get the absolute value to remove negative
					    sendingLocation[i] = last.Encryption(new BigInteger(String.valueOf( (int)orig[i] ))).toString(32);
					}
					//log.d("checkpoint", "Encrypted Location, sending to Alice now... " + (System.currentTimeMillis() - share.start));					
					//log.d("checkpoint", "Time between checking and beginning encryption " + (tmpEnd - tmpBegin));
					
					share.pKey = null; // Bob is done with this (GPS coordinates) key, throw it away
					
					// Build stringBuffer (the message body)
					txt = new StringBuffer();
					txt.append(sendingLocation[0]);
					txt.append(":" + sendingLocation[1]);
					
					// Latitude sign
					String sign = "+";
					if (l.getLatitude() < 0){
						sign = "-";
					}
					txt.append(":" + sign);
					
					// Longitude sign
					sign = "+";
					if (l.getLongitude() < 0){
						sign = "-";
					}
					txt.append(":" + sign);
					
					// Send current location to Alice
					p.sendFBMessage(sender, txt.toString(), 8, buff.session, context);
					break;
					// End of stage 7 (case 7)
					
				// Alice learns Bob's location
				case 8: // Stage 8 (case 8)
					Log.d("c", "Recieved Bob's location");
					//Decrypt and parse
					//log.d("ckeckpoint", "Starting Decryption of location " + (System.currentTimeMillis() - share.start));
					last = p.getKey(1024);
					double lat = last.Decryption(new BigInteger(parts[2], 32)).doubleValue();
					double lon = last.Decryption(new BigInteger(parts[3], 32)).doubleValue();
					
					// Clear key
					share = shareSingleton.getInstance();
					share.pKey = null; // Alice is done with this last (GPS coordinates) key, throw it away
					
					String latString = parts[4] + (lat/10000);
					String lonString = parts[5] + (lon/10000);
					
					// Done, present to user.
					long totalEnd = System.currentTimeMillis();
					Log.d("stats-alice", "Run is over: " + latString + ":" + lonString);
					share = shareSingleton.getInstance();
					Log.d("stats-alice", "Entire protocol took: " + (totalEnd - share.start));
					
					Intent i = new Intent(context, processedQueries.class);
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					i.putExtra("lat", latString);
					i.putExtra("lon", lonString);
					i.putExtra("name", sender);
					context.startActivity(i);
					
					/*
					// Notify Alice (myself)
					Intent notificationIntent = new Intent(context, processedQueries.class);
					notificationIntent.putExtra("lat", latString);
					notificationIntent.putExtra("lon", lonString);
					notificationIntent.putExtra("name", sender);
					notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
					String Title = sender + "'s Location";
					p.notification("Nearby Query Processed", Title, latString + ":" + lonString, context, notificationIntent);
					*/
					break;
					
				case 150: // this is used by the message test activity
					Log.d("nearbyListener:processMessage", "recieved test, sending back reply");
					p.sendFBMessage(sender, parts[2], 151, buff.session, context);
					break;
					
				case 151:
					share = shareSingleton.getInstance();
					double now = System.currentTimeMillis();
					Log.d("nearbyListener:processMessage", "Time spent: " + (now - share.messageTestStart));
					break;
					
				default:
					Log.d("nearbyListener", "DEFAULT SWITCH STATEMENT, SOMETHING BROKEN");
					break;
					
			} // End of switch
		}							
	} // end of Process Chat
	
	// Searches for a buffer by session number
	// This function avoids collisions by putting buffer objects into an arrayList of buffers
	// Return buffer if found, returns null otherwise
	private buffer searchBuff(String session){
		for (buffer b : buffs){		
			if (b.session.equals(session)){
				return b;
			}
		}
		return null;
	}
	
	
	// Clears a buffer (takes it out of the arrayList
	// Also, takes the trailing "@@" off the buffer
	// Returns the buffer object
	private void clearBuff(buffer b){
		for (int i = 0; i < buffs.size(); i++){
			if ( buffs.get(i) == b ){
				buffs.remove(i);
			}
		}
		b.message = b.message.substring(0, b.message.length() - 2); // Remove trailing "@@"
	}
	
	
	// Puts packets into ArrayList<buffer>.  Returns a buffer when it is full, returns null otherwise
	private buffer parseIncoming(Message packet){		
		
		boolean lastPacket = false;
		if ( packet.getBody().substring(0, 2).equals("@@") ){
			//String message = packet.getBody();
			//int stage = Integer.valueOf(packet.getBody().substring(2,3));
			String[] parts = packet.getBody().split(":");
			String session = parts[1];
			buffer buff;
			buff = searchBuff(session);
			if (buff == null){ // If the buffer is null we haven't seen this session yet and create a new buffer
				String sender = xmppService.getRoster().getEntry(packet.getFrom().toString()).getName();
				buff = new buffer(sender, System.currentTimeMillis(), session);
				buffs.add(buff);
				buff.append(packet.getBody().substring(2, 10));
			}
			String tmpMess = packet.getBody().substring(10); // Removing the beginning "@@X:sessi:" in message
			buff.append(tmpMess);
			lastPacket = packet.getBody().substring( packet.getBody().length() - 2 ).equals("@@"); // Packet ends in "@@"
			if ( lastPacket ){
				clearBuff(buff);
				return buff;
			}
		}
		// This is true of the most recent packet contained the end of sequence symbol at the end
		// buff.message.substring(buff.message.length() - 2).equals("@@");
		return null;
	}
}
