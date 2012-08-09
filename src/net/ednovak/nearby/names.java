package net.ednovak.nearby;

import java.util.ArrayList;
import java.util.Collection;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class names extends Activity{
	
    // To bind to service
    private ServiceConnection mConnection = new ServiceConnection() {
    	@Override
    	public void onServiceConnected(ComponentName className, IBinder service) {
    		//LocalBinder binder = (LocalBinder) service;
    		fillList(xmppService.getRoster());
    	}
    	
    	@Override
    	public void onServiceDisconnected(ComponentName name){
    		// Do Notta
    	}
    };
	
	@Override
	public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_names);
		
		Log.d("names", "binding to service");
		Intent bindIntent = new Intent(this, xmppService.class);
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		String user = sp.getString("fb_user", "None");
		String pass = sp.getString("fb_pass", "None");
		bindIntent.putExtra("user", user);
		bindIntent.putExtra("pass", pass);
		bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);		

	}
	
	public void onDestroy(){
		super.onDestroy();
		unbindService(mConnection);
	}
	
	public void fillList(Roster roster){
		ListView main = (ListView)findViewById(R.id.mainListView);
	                                      
	    Collection<RosterEntry> entries = roster.getEntries();
	    ArrayList<String> entriesList = new ArrayList<String>();
	    for (RosterEntry entry : entries){
	    	entriesList.add(entry.getName());
	    }
	      
	    // Create ArrayAdapter using the planet list.  
	    ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow, entriesList);
	    
	    
	    /*
	    // Add more planets. If you passed a String[] instead of a List<String>   
	    // into the ArrayAdapter constructor, you must not add more items.   
	    // Otherwise an exception will occur.  
	    listAdapter.add( "Ceres" );  
	    listAdapter.add( "Pluto" );  
	    */
	    
	    // Set the ArrayAdapter as the ListView's adapter
		main.setAdapter( listAdapter );
		main.setOnItemClickListener(new OnItemClickListener () {			
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id){
				String s = ((TextView)view).getText().toString();
				Log.d("names", "The thing clicked on: " + s);
				Intent rIntent = new Intent();
				rIntent.putExtra("other_user", s);
				setResult(RESULT_OK, rIntent);
				finish();
			}	 
		});
		
	}
	


}
