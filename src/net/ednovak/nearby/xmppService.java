package net.ednovak.nearby;

import java.util.Collection;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.RosterEntry;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

public class xmppService extends Service {
	
	public Connection conn;
	public Collection<RosterEntry> entries;
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
	
	public void sendMessage(String rec, String msg){
		// Send a message
		
	}
	
	@Override
	// I don't want to allow binding so the service keeps running in order to 
	// accept xmpp messages in the future
	public IBinder onBind(Intent intent){
		Toast.makeText(this, "service bound to", Toast.LENGTH_LONG).show();
		return xmppBinder;
	}
	
	
	// Use this to give the activity access to the public methods in this service.
	public class LocalBinder extends Binder {
		xmppService getService() {
			return xmppService.this;
		}
	}
}
