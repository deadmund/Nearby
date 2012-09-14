package net.ednovak.nearby;

import java.math.BigInteger;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import java.util.ArrayList;

public class xmppThread extends xmppService implements Runnable {
	
	private ArrayList<buffer> buffs = new ArrayList<buffer>();
	private int stage = 0;
	private String username;
	private String password;
	private Context context;
	//private long start;
	//public Collection<RosterEntry> entries;
	//public Connection conn;
	
	
	public xmppThread(String nUsername, String nPassword, Context nContext){
		username = nUsername;
		password = nPassword;
		context = nContext;		
		buffs = new ArrayList<buffer>(); // Blank the buffers
	}
	
	
	
	// Return buffer if found, returns null otherwise
	private buffer searchBuff(int session){
		for (buffer b : buffs){
			if (b.session == session){
				return b;
			}
		}
		return null;
	}
	
	
	
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
			String message = packet.getBody();
			int stage = Integer.valueOf(packet.getBody().substring(2,3));
			String[] parts = packet.getBody().split(":");
			int session = Integer.valueOf(parts[1]);
			buffer buff;
			buff = searchBuff(session);
			if (buff == null){ // If the buffer is null we haven't seen this session yet and create a new buffer
				String sender = getRoster().getEntry(packet.getFrom().toString()).getName();
				buff = new buffer(sender, System.currentTimeMillis(), session);
			}
			buff.append(packet.getBody().substring(4)); // Remove the begging "@@X:"
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
	
	
	public void run(){
		Connection connection = null;
		try{
			ConnectionConfiguration config = new ConnectionConfiguration("chat.facebook.com", 5222);
			config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);    				
    		connection = new XMPPConnection(config);
    		connection.connect();
    		connection.login(username, password);
    		Log.d("chat", "logged in as: " + username);
    		//Toast.makeText(context, "Logged Into Facebook", Toast.LENGTH_SHORT).show();
    		in = true;
    		conn = connection;
    		
    	} // End of try block
    	catch (XMPPException e){
    		Log.d("chat", "Caught Exception");
    		Log.d("chat", e.toString());
    	}  
		
		// Listen for incoming Messages
		ChatManager cManager = connection.getChatManager();
		cManager.addChatListener(new ChatManagerListener() {
			public void chatCreated(Chat chat, boolean createdLocally){
				if (!createdLocally || createdLocally){
					chat.addMessageListener(new MessageListener() {
						@Override
						public void processMessage(Chat chat, Message message) {
							//Log.d("xmpp", "Chat recieved in thread: " + message.getBody());
							Log.d("xmpp", "Chat recieved in thread");
							
							buffer buff = parseIncoming(message);
							
							// Message stream over, time to process this buffer
							if ( buff != null ) {
								long end = System.currentTimeMillis();
								long total_recTime = end - buff.start;
								Log.d("stats", "It took: " + total_recTime + "ms to recieve all chunks");
								
								// I can use sender later to fix the collision problem
								String sender = buff.sender;
								String[] parts = buff.message.split(":");
								
								Log.d("new", "Message: " + buff.message);
								for (String p: parts){
									Log.d("parts:", p);
								}

								protocol p = new protocol();
								shareSingleton share = shareSingleton.getInstance();								
								
								switch (stage){
									case 1: // This is Bob, stage 2										
										// Set up variables to call p.Bob
										Location location = p.locSimple(context);
										int pol = Integer.valueOf( parts[parts.length - 5] );
								        int bits = Integer.valueOf( parts[parts.length - 4] );
								        BigInteger g = new BigInteger( parts[parts.length - 3], 16 );
								        BigInteger n = new BigInteger( parts[parts.length - 2], 16 );
								        int method = Integer.valueOf( parts[parts.length - 1] );						        
								        
								        //Log.d("stage " + stage, "Generate and send Bob's message");
								        // Call Bob's function generate new message
										String txt = p.Bob(2, parts, pol, bits, g, n, method, location);
										//Log.d("xmpp", "txt in Bob: " + txt);
									
										// Send Bob's C values
								    	p.sendFBMessage(sender, txt, 2, 5, context);
										break;
									case 2:  // This is Alice, stage 3 (repeat of stage 1)
										// Check the incoming C's
										boolean found = p.check(parts, context);
										share.longitude = found;

										/*
										// Maybe we shouldn't show this now...
										if ( !found ) {
											Intent intent = new Intent(context, answerAct.class);
											intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
											intent.putExtra("answer", "Bob is not located near you!");
											intent.putExtra("found", found);
											context.startActivity(intent);
											
										}
										*/

										// Continue the protocol anyway so Bob doesn't catch wise.
										txt = p.alice(3, share.pol, share.bits, share.method); // txt is used in the above .Bob call
										Log.d("xmpp", "Done checking continuing protocol");
										p.sendFBMessage(sender, txt, 3, 6, context);
										
										// This is in a thread so the update is not synchronous
										// Not sure what a good solution is, I would love a toast...
										//TextView tv = (TextView)share.waiting.findViewById(R.id.text_view);
										//Log.d("stage" + 3, "tv's text: " + tv.getText().toString());
										//tv.setText("thing");
										//tv.setText(tv.getText().toString());
										//tv.append("Done checking longitude, checking latitude now...\n");
										//Log.d("stage" + 3, "tv's text: " + tv.getText().toString());
										break;
										
									case 3: // This is Bob, stage 4 (repeat of stage 2)
										// Set up variables to call p.Bob
										// This have all been instantiated in stage 2 (case 1)
										// This happens to be in this file
										location = p.locSimple(context);
										pol = Integer.valueOf( parts[parts.length - 5] );
								        bits = Integer.valueOf( parts[parts.length - 4] );
								        g = new BigInteger( parts[parts.length - 3], 16 );
								        n = new BigInteger( parts[parts.length - 2], 16 );
								        method = Integer.valueOf( parts[parts.length - 1] );						        
								        
								        //Log.d("stage " + stage, "Generate and send Bob's message");
								        // Call Bob's function generate new message
										txt = p.Bob(4, parts, pol, bits, g, n, method, location);
										//Log.d("xmpp", "txt in Bob: " + txt);
									
										// Send Bob's C values
								    	p.sendFBMessage(sender, txt, 4, 7, context);
								    	break;
								    	
									case 4: // This is Alice, stage 5 (final check of latitude)
										// Check the incoming C's
										// Found only holds the value of longitude || latitude NOT both.
										// For example, found maybe be true here if the latitude's match but the
										// longitudes do not.
										share.latitude =  p.check(parts, context);
										boolean near = share.latitude && share.longitude;
										Intent intent = new Intent(context, answerAct.class);
										intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
										intent.putExtra("found", near);
										
										if ( near ) {
											intent.putExtra("answer", "Bob is near you!");
										}
										else {
											intent.putExtra("answer", "Bob is not located near you!");
										}
										
										end = System.currentTimeMillis();
										share = shareSingleton.getInstance();
										long totalProtocol = end - share.start;
										Log.d("stats", "Total protocol time for Alice: " + totalProtocol + "ms");
										
										context.startActivity(intent);
										break;
								} // End of switch
							}							
						}
					}); // End of addMessageListener
				}
			}
		}); // End of addChatListener

	} // End of run()
}// End of thread class
		
		
