package net.ednovak.nearby;

import java.util.ArrayList;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import android.content.Context;
import android.util.Log;

public class xmppThread extends xmppService implements Runnable {
	
	private static ArrayList<buffer> buffs = new ArrayList<buffer>();
	//private int stage = 0;
	private String username;
	private String password;
	private Context context; // Context from whatever called this thread
	//private long start;
	//public Collection<RosterEntry> entries;
	//public Connection conn;
	
	
	public xmppThread(String nUsername, String nPassword, Context nContext){
		username = nUsername;
		password = nPassword;
		context = nContext;		
		buffs = new ArrayList<buffer>(); // Blank the buffers
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
					chat.addMessageListener(new nearbyListener(context, buffs)); // End of addMessageListener
				}
			}
		}); // End of addChatListener

	} // End of run()
}// End of thread class
		
		
