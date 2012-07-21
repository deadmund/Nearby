package net.ednovak.nearby;

import java.math.BigInteger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	public final static String EXTRA_MESSAGE = "net.ednovak.nearby.MESSAGE";
	private final static lListener myListener = new lListener();
	private LocationManager lManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);  
        
        lManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        lManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10000, myListener);
        // Turn on the following for a physical phone, turn it off for emulated device
        //lManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, lListener);

        
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
    }
    
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
    
    
    
    public void query(View view) {
        Intent intent = new Intent(this, displayMessageAct.class);
        shareSingleton share = shareSingleton.getInstance();
        
        // User's policy
        SeekBar sk = (SeekBar)findViewById(R.id.seekBar);
        int distance = sk.getProgress();
        distance = (int)((29.9 * distance) + 10);
        distance = Math.round(distance / 10) * 10;
        share.pol = distance;
        
        // recipient number
        EditText editText = (EditText) findViewById(R.id.other_user);
        String number = editText.getText().toString();
        share.number = number;
        
        Context context = getApplicationContext();
        if ( number.length() != 0 && number != null ){
        	if (!myListener.listening() ){
        		lManager.removeUpdates(myListener);
        		// I used to pass stuff in over the intent but using the shareSingleton is
        		// easy to code
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
    
    public void goToFacebook(View view){
    	Intent intent = new Intent(this, sendMessageFB.class);
    	startActivity(intent);
    }
}
