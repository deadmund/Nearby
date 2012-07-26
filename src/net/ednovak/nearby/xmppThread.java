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
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class xmppThread extends xmppService implements Runnable {
	
	private String buff = "";
	private int stage = 0;
	private String username;
	private String password;
	private Context context;
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
    		conn = connection;
    		roster = connection.getRoster();
    		
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
							Log.d("xmpp", "Chat recieved in thread: " + message.getBody());
							
							// If the protocol is being queried twice at once there is a big problem
							if ( message.getBody().substring(0, 2).equals("@@") ){
								buff += message.getBody().substring(4); // Remove the begging "@@X:"
								stage = Integer.valueOf(message.getBody().substring(2, 3));
								//Log.d("xmpp", "state of buff: " + buff + "    state: " + stage);
							}

							
							// Message stream over, time to process this message
							if ( buff.substring(buff.length() - 2).equals("@@")) {
								//Log.d("xmpp", "emptying and processing buff!");
								buff = buff.substring(0, buff.length() - 2); // Remove the trailing "@@"
								
								// I can use sender later to fix the collision problem
								String sender = roster.getEntry(message.getFrom().toString()).getName();
								String[] parts = buff.split(":");
								buff = "";
	

								protocol p = new protocol();
								switch (stage){
									case 1:
										
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
										Log.d("xmpp", "txt in Bob: " + txt);
									
										// Send Bob's C values
								    	p.sendFBMessage(sender, txt, 2, context); // This came in off xmpp
								    	
										break;
									case 2:
										// Check the incoming C's
										//p.check(tokens, context);	
								}
							}							
						}
					}); // End of addMessageListener
				}
			}
		}); // End of addChatListener

	} // End of run()
}// End of thread class
		
		
