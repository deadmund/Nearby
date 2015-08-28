package net.ednovak.nearby;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
	
	public final static String EXTRA_MESSAGE = "net.ednovak.nearby.MESSAGE";
	private lListener myListener = new lListener();
	private LocationManager lManager;
	private logInReceiver rec;
	private ToggleButton chatButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Location listener stuff
        lManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        lManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10000, myListener);
        // Turn on the following for a physical phone, turn it off for emulated device
        try {
        	lManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10000, myListener);
        }
        catch (Exception e){
        	// Well nevermind then!
        }
        
        // Grab reference to chat button
        chatButton = (ToggleButton)findViewById(R.id.fb_chat);
    } // End of onCreate
    
    
    // To update name based on names activity (startActivityForResult
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
    	switch (requestCode) {
    	case 1:
    		if ( resultCode == RESULT_OK){
    			String name = intent.getStringExtra("other_user");
    			EditText rec = (EditText) findViewById(R.id.other_user);
    			rec.setText(name);
    			break;
    		}
    	}
    	
    }
    
    
    // Creates the menu (from pressing menu button on main screen)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        return true;
    }
    
    
    // When the settngs button is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item){  	
    	
    	Log.d("main", "Selected: " + item.toString());
    	switch ( item.getItemId() ){
    		case R.id.settings:
    			startActivity(new Intent(this, settings.class));
    			return true;
    			
    		case R.id.names:
    			if (xmppService.in){
    				startActivityForResult(new Intent(this, names.class), 1);
    				return true;
    			}
    			else{
					Toast.makeText(this, "You must be logged into Facebook", Toast.LENGTH_SHORT).show();
					return false;
    			}
    			
    		case R.id.test_encryption:
    	    	startActivity(new Intent(this, paillierTest.class));
    	    	return true;
    	    	
    		case R.id.test_message: // Test sending messages on FB
    			if (xmppService.in){
    				startActivity(new Intent(this, messageTest.class));
    				return true;
    			}
    			else{
					Toast.makeText(this, "You must be logged into Facebook", Toast.LENGTH_SHORT).show();
					return false;
    			}

            case R.id.bf_test:
                Intent i = new Intent(this, BloomFilterTest.class);
                startActivity(i);
                return true;
    		
    		default:
    			return super.onOptionsItemSelected(item);
    	}
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	
    	if (rec == null){
    		// To connect the onRecieve
    		IntentFilter logInFilter;
    		logInFilter = new IntentFilter(xmppService.LOGIN_UPDATE);
    		rec = new logInReceiver();
    		registerReceiver(rec, logInFilter);

    		//The example has a call here: startxmppService();
    	}
    	
    }
    
    
    // When the activity ends
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	if ( xmppService.in ){
    		unbindService(mConnection);
    	}
    	try{ // Sometimes the app is distroyed without the logInRec ever having been registered
    		unregisterReceiver(rec); 
    	}
    	catch(IllegalArgumentException e){
    		// Do nothing
    	}
    }
    
    
    // Query Button
    public void query(View view) {
        Intent intent = new Intent(this, displayMessageAct.class);
        shareSingleton share = shareSingleton.getInstance();
        share.start = System.currentTimeMillis();
        
        // recipient number / name
        EditText otherUser = (EditText) findViewById(R.id.other_user);
        String rec = otherUser.getText().toString();
        share.rec = rec; // need this in the service receive
        
        //Context context = getApplicationContext();
        Context ctx = view.getContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Log.d("main", "contains it: " + prefs.contains("fake_locations"));
        boolean it = prefs.getBoolean("fake_locations", false);
        //Log.d("main", "prefs.getBoolean(\"fake_locations\"): " + it);
        if ( it ){
        	//Log.d("main", "Fake locations turned on; plugging the fake one!");
        	myListener.plugFake(ctx);
        }
        if ( rec.length() != 0 && rec != null ){
        	if ( !myListener.listening() ){
        		lManager.removeUpdates(myListener);
        		// I used to pass stuff in over the intent but using the shareSingleton is
        		// allows me to access these values in the on receive later
        		protocol p = new protocol();
        		Location l = p.locSimple(this);
        		share.lon = l.getLongitude(); // Storing Alice's location, Alice initiates queries
        		share.lat = l.getLatitude();
        		
        		intent.putExtra("rec", rec);
        		startActivity(intent);
        	}
    		else { // Don't have a good location lock yet
    			Toast.makeText(ctx, "Still waiting for a lock on your location", Toast.LENGTH_SHORT).show();
    		}
        }
        else { // Don't have the phone number entered!
        	Toast.makeText(ctx, "You need to provide a recipient", Toast.LENGTH_SHORT).show();
        }
    }
    
    
    // To bind to service
    private ServiceConnection mConnection = new ServiceConnection() {
    	@Override
    	public void onServiceConnected(ComponentName className, IBinder service) {
    		// Nada!
    	}
    	
    	@Override
    	public void onServiceDisconnected(ComponentName name){
    		chatButton.setChecked(false);
    	}
    };
    
    
    // Toggle Button at the bottom
    public void fbChatConnect(View view){
    	
    	if (xmppService.in){
    		Log.d("main", "Turning service off");
    		unbindService(mConnection);
    		stopService(new Intent(this, xmppService.class));
    		chatButton.setChecked(false);
    	}
    	
    	else {
    		chatButton.setChecked(false);
    		chatButton.setText("Logging In...");
    		Log.d("main", "turning service on");
    		Intent bindIntent = new Intent(this, xmppService.class);
    		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
    		String user = sp.getString("fb_user", "None");
    		String pass = sp.getString("fb_pass", "None");
    		bindIntent.putExtra("user", user);
    		bindIntent.putExtra("pass", pass);
    		bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);   		
    	}
    }


    // For some reason this is defined in the activity class.  I though it worked differently in the owl project
	public class logInReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent){
			boolean light = intent.getBooleanExtra("connection", false);
			chatButton.setChecked(light);
		}
	}
}