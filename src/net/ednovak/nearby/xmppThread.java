package net.ednovak.nearby;

import java.util.Collection;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

import android.util.Log;

public class xmppThread extends xmppService implements Runnable {
	
	private String username;
	private String password;
	//public Collection<RosterEntry> entries;
	//public Connection conn;
	
	public xmppThread(String nUsername, String nPassword){
		username = nUsername;
		password = nPassword;
	}
	
	public void run(){
		Connection connection = null;
		try{
			ConnectionConfiguration config = new ConnectionConfiguration("chat.facebook.com", 5222);
			config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);    				
    		connection = new XMPPConnection(config);
    		Log.d("chat", "Made a new connection object");
    		connection.connect();
    		Log.d("chat", "Connection made");
    		connection.login(username, password);
    		Log.d("chat", "logged in!");
    		conn = connection;
    		roster = connection.getRoster();
    		
    	} // End of try block
    	catch (XMPPException e){
    		Log.d("chat", "Caught Exception");
    		Log.d("chat", e.toString());
    	}  
    		

		
		// A Roster listener
		roster.addRosterListener(new RosterListener() {
			public void entriesAdded(Collection<String> addresses) {
				Log.d("xmpp", "Entry added");
				//entries = roster.getEntries();
			}
			
			public void entriesDeleted(Collection<String> addresses) {
				Log.d("xmpp", "Entry deleted");
				//entries = roster.getEntries();
			}
			
			public void entriesUpdated(Collection<String> addresses) {
				Log.d("xmpp", "Entries Updated");
				//entries = roster.getEntries();
			}
			
			public void presenceChanged(Presence presence){
				Log.d("xmpp", "presence changed " + presence.getFrom() + " " + presence);
				//entries = roster.getEntries();
			} 			
		});
		
		// Listen for incoming Messages
		ChatManager cManager = connection.getChatManager();
		cManager.addChatListener(new ChatManagerListener() {
			public void chatCreated(Chat chat, boolean createdLocally){
				if (!createdLocally){
					chat.addMessageListener(new MessageListener() {
						@Override
						public void processMessage(Chat chat, Message message) {
							Log.d("xmpp", "Chat recieved: " + message.getBody());
						}
					}); // End of addMessageListener
				}
			}
		}); // End of addChatListener

	} // End of run()
}// End of thread class
		
		
