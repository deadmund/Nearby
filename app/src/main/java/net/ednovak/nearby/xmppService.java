package net.ednovak.nearby;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class xmppService extends Service {
	
	public static final String LOGIN_UPDATE = "net.ednovak.nearby.xmppService.action.LOGIN_UPDATE";
	public static Connection conn;
	public static Boolean in = false;
	IBinder xmppBinder = new LocalBinder();
	
	@Override
	public void onCreate(){
		// I think have nothing to do here.
		super.onCreate();
		//Toast.makeText(this, "service created", Toast.LENGTH_LONG).show();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		if ( conn != null ){
			conn.disconnect();
			conn = null;
			in = false;
		}
		//Toast.makeText(this, "fb chat service destroyed", Toast.LENGTH_LONG).show();
	}	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startID){
		// I think I should start a thread here
        // XMPP listener
		
		String user = intent.getStringExtra("user");
		String pass = intent.getStringExtra("pass");
		
        Runnable r = new xmppThread(user, pass, getApplicationContext());
        new Thread(r).start();
        //Toast.makeText(this, "thread created and ran", Toast.LENGTH_LONG).show();
        
        return START_REDELIVER_INTENT;
	}
	
	
	// Splits a long string up into several 'packets' so the fb does not filter them
	// Each packet beginning @@<stage number>:sessionnumber:
	// and, on the last packet in the stream, a trailing @@
	private static List<String> make_packets(String msg, int stage, String session, int chunk){
		//Log.d("xmpp", "Chunk size set to: " + chunk);
		
		//Log.d("stage " + stage, "Dividing this string: " + msg);
		List<String> packets = new ArrayList<String>();
		int cur = 0;
		int end = 0;
		int total = 0; // Total number of packets created
		while (end < msg.length()){
			total += 1;
			end = Math.min(msg.length(), cur + chunk);
			//Log.d("xmpp", "Adding chunk from " + cur + " to " + end);
			packets.add("@@" + stage + ":" + session + ":" + msg.substring(cur, end));
			cur = end;
		}
		// I use the "@@" on the last packet to mark the end of a stream
		packets.set(packets.size()-1, packets.get(packets.size()-1) + "@@"); // Put a "@@" on the last one
		Log.d("stats", "Total packets created: " + total);
		return packets;
	}
	
	
	// Send message that actually does the sending
	private static void real_send(Chat chat, String msg, int stage, String session, int chunk){
		List<String> parts = make_packets(msg, stage, session, chunk);
		for(int i = 0; i < parts.size(); i++){
			//Log.d("xmpp", "part: " + parts.get(i));
			try{
				//Log.d("test", "Sending Message Chunk: " + parts.get(i));
				chat.sendMessage(parts.get(i));
			}
			catch (XMPPException e){
				Log.d("xmpp", "Caught exception: " + e.toString());
			}
		}	
		Log.d("xmpp", "Message sent");
	}
	
	
	// Send message exposed to real world
	public static void sendMessage(String rec, String msg, int stage, String session, final Context context){
		//Log.d("xmpp", "Looking for: " + rec);
		Collection<RosterEntry> entries = getRoster().getEntries();
		if (entries != null){
			for (RosterEntry entry : entries){
				//Log.d("chat", ""+entry);
				if (entry.getName().equals(rec)){
					//Log.d("xmpp", "found this person: " + entry.getName() + " " + entry.getUser() + " " + entry.getStatus());
					Chat newChat = conn.getChatManager().createChat(entry.getUser(), null);
					Log.d("xmpp", "sending message to: " + entry.getName());
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
					int chunk = Integer.valueOf(prefs.getString("chunk", "500"));
					real_send(newChat, msg, stage, session, chunk);
				}
			}
		}
		else{
			Log.d("xmpp", "entries was null");
		}
	}
	
	@Override
	// I don't want to allow binding so the service keeps running in order to 
	// accept xmpp messages in the future
	public IBinder onBind(Intent intent){
		//Toast.makeText(this, "service bound to", Toast.LENGTH_LONG).show();
		
		if ( conn == null ){
			String user = intent.getStringExtra("user");
			String pass = intent.getStringExtra("pass");
			
	        Runnable r = new xmppThread(user, pass, this);
	        new Thread(r).start();
	        //Toast.makeText(this, "thread created and ran", Toast.LENGTH_LONG).show();
		}
		
		return xmppBinder;
	}
	
	
	// Use this to give the activity access to the public methods in this service.
	public class LocalBinder extends Binder {
		xmppService getService() {
			return xmppService.this;
		}
	}
	
	public static Roster getRoster(){
		return conn.getRoster();
	}
	
	private void announceLogin(boolean in){
		Intent intent = new Intent(LOGIN_UPDATE);
		intent.putExtra("connection", in);
		
		sendBroadcast(intent);	
	}
	
	// This is now an inner class
	public class xmppThread implements Runnable {
		
		//private ArrayList<buffer> buffs = new ArrayList<buffer>();
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
			//buffs = new ArrayList<buffer>(); // Blank the buffers
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
	    		in = false;
	    		Log.d("chat", "Caught Exception");
	    		Log.d("chat", e.toString());
	    	}  
			
    		announceLogin(in);
    		
			
			// Listen for incoming Messages
			ChatManager cManager = connection.getChatManager();
			cManager.addChatListener(new ChatManagerListener() {
				public void chatCreated(Chat chat, boolean createdLocally){
					if (!createdLocally || createdLocally){
						chat.addMessageListener(new nearbyListener(context)); // End of addMessageListener
					}
				}
			}); // End of addChatListener

		} // End of run()
	}// End of thread class
}
