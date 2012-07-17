package net.ednovak.nearby;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
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
        	
    }
    
    @Override
    public void onResume() {
    	super.onResume();
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
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    */
    
    
    public void query(View view) {
        Intent intent = new Intent(this, displayMessageAct.class);
        
        // User's location
        double lon = myListener.lon;
        double lat = myListener.lat;
        
        // User's policy
        SeekBar sk = (SeekBar)findViewById(R.id.seekBar);
        int distance = sk.getProgress();
        distance = (int)((29.9 * distance) + 10);
        distance = Math.round(distance / 10) * 10;
        
        // recipient number
        EditText editText = (EditText) findViewById(R.id.other_user);
        String number = editText.getText().toString();
        
        Context context = getApplicationContext();
        if ( number.length() != 0 && number != null ){
        	if (!myListener.listening() ){
        		lManager.removeUpdates(myListener);
        		intent.putExtra("lon", lon);
        		intent.putExtra("lat", lat);
        		intent.putExtra("policy", distance);
        		intent.putExtra("number", number);
        		startActivity(intent);
        	}
    		else {
    			Toast.makeText(context, "Still waiting for a lock on your location", Toast.LENGTH_SHORT).show();
    		}
        }
        else {
        	Toast.makeText(context, "You need to provide a phone number", Toast.LENGTH_SHORT).show();
        }
    }
}
