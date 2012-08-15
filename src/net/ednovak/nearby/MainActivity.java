package net.ednovak.nearby;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity {
	
	public final static String EXTRA_MESSAGE = "net.ednovak.nearby.MESSAGE";
	private lListener myListener = new lListener();
	private LocationManager lManager;
	private EditText other_user;
	private boolean bound = false;
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

        // Seek bar
        SeekBar sk = (SeekBar)findViewById(R.id.seekBar);
        final TextView tv = (TextView)findViewById(R.id.seekbar_text);
        
        sk.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
        	
        	public void onStopTrackingTouch(SeekBar sk){
        		// Nothing yet
        	}
        	
        	public void onStartTrackingTouch(SeekBar sk){
        		// Nothing yet
        	}
        	
        	public void onProgressChanged(SeekBar sk, int progress, boolean isUser){
        		int value = (int)((29.9 * progress) + 10);
        		value = Math.round(value / 10) * 10;
        		tv.setText(Integer.toString(value) + " meters");
        	}
        });

        // Other user ID
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String message_type = prefs.getString("message_type", "fb");
        if ( message_type.equals("fb") ){
        	EditText et = new EditText(this);
        	other_user = et;
        	et.setHint("Facebook Friend's Name");        	
        	LinearLayout ll = (LinearLayout)findViewById(R.id.ll);
        	ll.addView(et, 3);
        }
        
        else if ( message_type.equals("sms") ) {
        	EditText et = new EditText(this);
        	other_user = et;
        	et.setHint("Phone Number");
        	LinearLayout ll = (LinearLayout)findViewById(R.id.ll);
        	ll.addView(et, 3);
        }
        
        else {
        	Log.d("main", "Bad option choice " + message_type);
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
    			other_user.setText(name);
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
    			if (bound && xmppService.in){
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
    			if (bound && xmppService.in){
    				startActivity(new Intent(this, messageTest.class));
    				return true;
    			}
    			else{
					Toast.makeText(this, "You must be logged into Facebook", Toast.LENGTH_SHORT).show();
					return false;
    			}
    			
    		
    		default:
    			return super.onOptionsItemSelected(item);
    	}
    }
    
    @Override
    public void onResume(){
    	super.onResume();
        // Other user ID
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String message_type = prefs.getString("message_type", "fb");
        if ( message_type.equals("fb") ){
        	other_user.setHint("Facebook Friend's Name");
        }
        else if ( message_type.equals("sms") ) {
        	other_user.setHint("Phone Number");
        }    
        else {
        	Log.d("main", "Bad option choice " + message_type);
        }
    }
    
    
    // When the activity ends
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	if ( bound ){
    		unbindService(mConnection);
    	}
    }
    
    
    // Query Button
    public void query(View view) {
        Intent intent = new Intent(this, displayMessageAct.class);
        shareSingleton share = shareSingleton.getInstance();
        share.start = System.currentTimeMillis();
        
        // User's policy
        SeekBar sk = (SeekBar)findViewById(R.id.seekBar);
        int distance = sk.getProgress();
        distance = (int)((29.9 * distance) + 10);
        distance = Math.round(distance / 10) * 10;
        share.pol = distance; // need this in the service receive
        
        // recipient number / name
        EditText editText = other_user;
        String rec = editText.getText().toString();
        share.rec = rec; // need this in the service receive
        
        Context context = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Log.d("main", "contains it: " + prefs.contains("fake_locations"));
        boolean it = prefs.getBoolean("fake_locations", false);
        Log.d("main", "prefs.getBoolean(\"fake_locations\"): " + it);
        if ( it ){
        	Log.d("main", "Fake locations turned on; plugging the fake one!");
        	myListener.plugFake(context);
        }
        if ( rec.length() != 0 && rec != null ){
        	if ( !myListener.listening() ){
        		lManager.removeUpdates(myListener);
        		// I used to pass stuff in over the intent but using the shareSingleton is
        		// allows me to access these values in the on receive later
        		protocol p = new protocol();
        		Location l = p.locSimple(this);
        		share.lon = l.getLongitude();
        		share.lat = l.getLatitude();
        		
        		intent.putExtra("rec", rec);
        		intent.putExtra("pol", distance);
        		startActivity(intent);
        	}
    		else { // Don't have a good location lock yet
    			Toast.makeText(context, "Still waiting for a lock on your location", Toast.LENGTH_SHORT).show();
    		}
        }
        else { // Don't have the phone number entered!
        	Toast.makeText(context, "You need to provide a phone number", Toast.LENGTH_SHORT).show();
        }
    }
    
    
    // To bind to service
    private ServiceConnection mConnection = new ServiceConnection() {
    	@Override
    	public void onServiceConnected(ComponentName className, IBinder service) {
    		//LocalBinder binder = (LocalBinder) service;
    		//share.serv = binder.getService();
    		bound = true;
    		chatButton.setChecked(true);
    	}
    	
    	@Override
    	public void onServiceDisconnected(ComponentName name){
    		Log.d("main", "turning it off");
    		bound = false;
    		chatButton.setChecked(false);
    	}
    };
    
    
    // Toggle Button at the bottom
    public void fbChatConnect(View view){
    	
    	if (bound){
    		Log.d("main", "Turning service off");
    		unbindService(mConnection);
    		stopService(new Intent(this, xmppService.class));
    		bound = false;
    		((ToggleButton)view).setChecked(false);
    	}
    	
    	else {
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
}
