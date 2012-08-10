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
import android.widget.Toast;

public class xmppThread extends xmppService implements Runnable {
	
	private String buff = "";
	private int stage = 0;
	private String username;
	private String password;
	private Context context;
	private long start;
	//public Collection<RosterEntry> entries;
	//public Connection conn;
	
	public xmppThread(String nUsername, String nPassword, Context nContext){
		username = nUsername;
		password = nPassword;
		context = nContext;		
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
							
							
							// If the protocol is being queried twice at once there is a big collision problem
							if ( message.getBody().substring(0, 2).equals("@@") ){
								if (buff.equals("")){
									start = System.currentTimeMillis();
								}
								buff += message.getBody().substring(4); // Remove the begging "@@X:"
								stage = Integer.valueOf(message.getBody().substring(2, 3));
								//Log.d("xmpp", "state of buff: " + buff + "    state: " + stage);
							}

							
							// Message stream over, time to process this message
							if ( buff.substring(buff.length() - 2).equals("@@")) {
								long end = System.currentTimeMillis();
								long total_recTime = end - start;
								Log.d("stats", "It took: " + total_recTime + "ms to recieve all chunks");
								
								//Log.d("xmpp", "emptying and processing buff!");
								buff = buff.substring(0, buff.length() - 2); // Remove the trailing "@@"
								
								// I can use sender later to fix the collision problem
								String sender = getRoster().getEntry(message.getFrom().toString()).getName();
								String[] parts = buff.split(":");
								buff = "";
	

								protocol p = new protocol();
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
								    	p.sendFBMessage(sender, txt, 2, context);
								    	
										break;
									case 2:  // This is Alice, stage 3 (repeat of stage 1)
										// Check the incoming C's
										boolean found = p.check(parts, context);
										
										if ( !found ) {
											Intent intent = new Intent(context, answerAct.class);
											intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
											intent.putExtra("answer", "Bob is not located near you!");
											intent.putExtra("found", found);
											context.startActivity(intent);
										}

										// Continue the protocol anyway so Bob doesn't catch wise.
										shareSingleton share = shareSingleton.getInstance();
										txt = p.alice(3, share.pol, share.bits, share.method); // txt is used in the above .Bob call
										Log.d("xmpp", "Done checking continuing protocol");
										p.sendFBMessage(sender, txt, 3, context);
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
								    	p.sendFBMessage(sender, txt, 4, context);
								    	break;
								    	
									case 4: // This is Alice, stage 5 (final check of latitude)
										// Check the incoming C's
										found = p.check(parts, context);
										
										Intent intent = new Intent(context, answerAct.class);
										intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
										intent.putExtra("found", found);
										
										if ( found ) {
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
		
		
