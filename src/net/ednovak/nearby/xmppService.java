package net.ednovak.nearby;

import java.util.Collection;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class xmppService extends Service {
	
	public static Connection conn;
	//public static Collection<RosterEntry> entries;
	public static Roster roster;
	IBinder xmppBinder = new LocalBinder();
	
	@Override
	public void onCreate(){
		// I think have nothing to do here.
		super.onCreate();
		Toast.makeText(this, "service created", Toast.LENGTH_LONG).show();
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		conn.disconnect();
		Toast.makeText(this, "service destroyed", Toast.LENGTH_LONG).show();
	}	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startID){
		// I think I should start a thread here
        // XMPP listener
		
		String user = intent.getStringExtra("user");
		String pass = intent.getStringExtra("pass");
		
        Runnable r = new xmppThread(user, pass);
        new Thread(r).start();
        Toast.makeText(this, "thread created and ran", Toast.LENGTH_LONG).show();
        
        return START_REDELIVER_INTENT;
	}
	
	public void sendMessage(String rec, String msg, final Context context){
		Log.d("xmpp", "Looking for: " + rec);
		Collection<RosterEntry> entries = roster.getEntries();
		if (entries != null){
			for (RosterEntry entry : entries){
				//Log.d("chat", ""+entry);
				if (entry.getName().equals(rec)){
					Log.d("xmpp", "found this person: " + entry.getName() + " " + entry.getUser() + " " + entry.getStatus());
					Chat newChat = conn.getChatManager().createChat(entry.getUser(), new MessageListener() {
						public void processMessage(Chat chat, Message message){
							
							// Convert to regular token array (cause over sms there is a phone number
							String[] tmp = message.toString().split(":");
							String[] tokens = new String[tmp.length+1];
							for(int i = 1; i < tokens.length; i++){
								tokens[i] = tmp[i-1];
							}
							
							// Check the incoming C's
							protocol p = new protocol();
							p.check(tokens, context);							
						}
					});
					try {
						Log.d("xmpp", "sending message: " + msg + "  to: " + rec);
						
						newChat.sendMessage(msg);
					}
					catch (XMPPException e){
						Log.d("xmpp", "Error sending message");
					}
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
		Toast.makeText(this, "service bound to", Toast.LENGTH_LONG).show();
		
		if (conn == null){
			String user = intent.getStringExtra("user");
			String pass = intent.getStringExtra("pass");
			
	        Runnable r = new xmppThread(user, pass);
	        new Thread(r).start();
	        Toast.makeText(this, "thread created and ran", Toast.LENGTH_LONG).show();
		}
		
		return xmppBinder;
	}
	
	
	// Use this to give the activity access to the public methods in this service.
	public class LocalBinder extends Binder {
		xmppService getService() {
			return xmppService.this;
		}
	}
}
