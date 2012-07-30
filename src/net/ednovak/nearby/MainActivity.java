package net.ednovak.nearby;

import java.math.BigInteger;

import net.ednovak.nearby.xmppService.LocalBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
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

public class MainActivity extends Activity {
	
	public final static String EXTRA_MESSAGE = "net.ednovak.nearby.MESSAGE";
	private final static lListener myListener = new lListener();
	private LocationManager lManager;
	private EditText other_user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  
        
        // Location listener stuff
        lManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        lManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10000, myListener);
        // Turn on the following for a physical phone, turn it off for emulated device
        //lManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, lListener);

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
        SharedPreferences prefs = getSharedPreferences("preferences", 0);
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
        	et.setHint("Phone number");
        	LinearLayout ll = (LinearLayout)findViewById(R.id.ll);
        	ll.addView(et, 3);
        }
        
        else {
        	Log.d("main", "Bad option choice " + message_type);
        }
        
        // Used to start the service but binding allows me to stop it ever
        //Intent xmppIntent = new Intent(MainActivity.this, xmppService.class);
        //xmppIntent.putExtra("user", "ed.novak3");
        //xmppIntent.putExtra("pass", "fetusgo2");
        //startService(xmppIntent);
        
        Intent bindIntent = new Intent(this, xmppService.class);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String user = sp.getString("fb_user", "None");
        String pass = sp.getString("fb_pass", "None");
        bindIntent.putExtra("user", user);
        bindIntent.putExtra("pass", pass);
        bindService(bindIntent, mConnection, Context.BIND_AUTO_CREATE);
        //stopService(new Intent(MainActivity.this, xmppService.class));
        
        /* Big Fat Paillier Homomorphic Encryption Test
        // 
        Log.d("main.onCreate", "Testing paillier homomorphic properties");
        
        Paillier paillier = new Paillier();
		BigInteger ma = new BigInteger("5");
		BigInteger mb = new BigInteger("6");
		BigInteger mc = new BigInteger("7");
		BigInteger ema = paillier.Encryption(ma);
		BigInteger emb = paillier.Encryption(mb);
		//BigInteger emc = paillier.Encryption(mc);
		
						//  (ema      *  (          (emb^mc) % n         ))    % n^2      
		BigInteger answer = (ema.multiply(emb.modPow(mc, paillier.nsquare))).mod(paillier.nsquare);
		
		Log.d("main", "Should be 47 " + paillier.Decryption(answer));
		
		protocol p = new protocol();
		BigInteger test1 = p.homoAdd(ema, emb, paillier.n);
		BigInteger test2 = p.homoMult(ema, mb, paillier.n);
		BigInteger test3 = p.homoExpo(ema, ma, new BigInteger("4"), paillier.n);
		Log.d("main", "test 1: " + paillier.Decryption(test1) + "   test 2: " + paillier.Decryption(test2)+ "   test 3: " + paillier.Decryption(test3));
		*/
    }
    
    // To bind to service
    private ServiceConnection mConnection = new ServiceConnection() {
    	@Override
    	public void onServiceConnected(ComponentName className, IBinder service) {
    		LocalBinder binder = (LocalBinder) service;
    		shareSingleton share = shareSingleton.getInstance();
    		share.serv = binder.getService();
    		
    		// Next step: move this into the protocol class
    		Log.d("main", "We are bound !");
    	}
    	
    	@Override
    	public void onServiceDisconnected(ComponentName name){
    		// Do notta
    	}
    };
    
    
    // Creates the menu (from pressing menu button on main screen)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        return true;
    }
    
    // When the settngs butotn is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item){  	
    	
    	switch ( item.getItemId() ){
    		case R.id.settings:
    			Log.d("menu", "It is working...");
    			startActivity(new Intent(this, settings.class));
    			return true;
    		
    		default:
    			return super.onOptionsItemSelected(item);
    	}
    }
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
    	unbindService(mConnection);
    }
    
    public void query(View view) {
        Intent intent = new Intent(this, displayMessageAct.class);
        shareSingleton share = shareSingleton.getInstance();
        
        // User's policy
        SeekBar sk = (SeekBar)findViewById(R.id.seekBar);
        int distance = sk.getProgress();
        distance = (int)((29.9 * distance) + 10);
        distance = Math.round(distance / 10) * 10;
        share.pol = distance; // need this in the service receive
        
        // recipient number
        EditText editText = other_user;
        String rec = editText.getText().toString();
        share.rec = rec; // need this in the service receive
        
        Context context = getApplicationContext();
        if ( rec.length() != 0 && rec != null ){
        	if (!myListener.listening() ){
        		lManager.removeUpdates(myListener);
        		// I used to pass stuff in over the intent but using the shareSingleton is
        		// allows me to access these values in the on receive later
        		//intent.putExtra("serv", serv); Can't pass this type of object around
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
    
    public void goToEncryptionTest(View view){
    	Intent intent = new Intent(this, paillierTest.class);
    	startActivity(intent);    	
    }
    
    public void test(View view){
    	Log.d("test", "Stub Function");
    	
    	// Create rep set
    	treeQueue repSet = new treeQueue();
    	repSet.push( new tree(2117648, null, null, null));
    	repSet.push( new tree(2117649, null, null, null));
    	
    	// Make coefficients
    	protocol p = new protocol();
    	BigInteger[] coefficients = p.makeCoefficients(repSet, 2);
    	// Print coefficients
    	for(int i = 0; i < coefficients.length; i ++){
    		Log.d("test", "coefficient: " + coefficients[i]);
    	}
    	
    	// Encrypt coefficients
    	Paillier pail = new Paillier(1024, 64);
    	BigInteger[] encCoe = new BigInteger[coefficients.length];
    	for(int i = 0; i < encCoe.length; i++){
    		encCoe[i] = pail.Encryption(coefficients[i]); 
    	}
    	// Print encrypted Coe
    	for(int i = 0; i < encCoe.length; i++){
    		Log.d("test", "enc coefficient: " + encCoe[i]);
    	}
    	
    	// Make covering set for Bob    	
    	treeQueue coveringSet = new treeQueue();
    	coveringSet.push( new tree(2117648, null, null, null) );
    	
    	// Do Bob's calculations
    	// Get the key
    	BigInteger[] pKey = pail.publicKey();
    	BigInteger g = pKey[0];
    	BigInteger n = pKey[1];
    	BigInteger[] results = p.bobCalc(coveringSet, encCoe, 1024, g, n, 2);
    	
    	// Decrypt and print results
    	for(int i = 0; i < results.length; i++){
    		Log.d("test", "result: " + pail.Decryption(results[i]));
    	}
    }
}
