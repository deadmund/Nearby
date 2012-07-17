package net.ednovak.nearby;

import java.math.BigInteger;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;

@SuppressWarnings("serial")
class longitudeException extends Exception{
	public longitudeException(String msg){
		super(msg);
	}
}

public class displayMessageAct extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait);
        
        /* dynamic content setting
        // Get intent
        //Intent intent = getIntent();
        //String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        
        // Create textview in java instead of XML and put the string in it
        TextView textView = new TextView(this);
        textView.setTextSize(40);
        textView.setText(message);
        
        setContentView(textView);
        */
        
        
        LocationManager lManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        
        LocationListener lListener = new LocationListener(){
        	public void onLocationChanged(Location location) { // The callback
        		Log.d("location", "The location I got: " + location);
        		if (location.hasAccuracy()){
        			if (location.getAccuracy() < 1.0){
        				Log.d("location", "This location is good enough");
        				// do it all really goes here
        			}
        		}
        		doItAll(location);
        	}
        	
        	public void onStatusChanged(String provider, int status, Bundle extra){
        		Log.d("location", "status changed");
        	}
        	
        	public void onProviderEnabled(String provider) {
        		Log.d("location", "providerEnabled");
        	}
        	
        	public void onProviderDisabled(String provider){
        		Log.d("location", "providerDisabled");
        	}
        };
        
        lManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10000, lListener);
        lManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, lListener);
    } // End of onCreate
    
    private void doItAll(Location loc){
        // New instance of the protocol
        protocol p = new protocol();
        
    	Intent intent = getIntent();
        int pol = intent.getIntExtra("policy", 10);
		// Alice's policy width and x location (longitude)
        int width = p.policyToWidth(pol); // These are user configurable
        int x = p.longitudeToInt(loc.getLongitude()); // just long for now
        
        System.out.println("Alice's x: " + x);
        
        int left = x - width;
        int right = x + width;
        System.out.println("Alice's leaf nodes go from " + left + " to " + right + ".  Her node is: " + x);
        //System.out.println("Lowest possible node on the tree at long=-180: " + this.longitudeToInt(-180.0));
        //System.out.println("Highest possible node on the tree at long=180: " + this.longitudeToInt(180.0));
        
        treeQueue leaves = p.genLeaves(left, right, x);
        
        System.out.println("Here are the leaves in Alice's span:");
        for (int i = 0; i < leaves.length; i++){
        	System.out.println("" + leaves.peek(i));
        	System.out.println(" ");
        }
        
        System.out.println("Building the tree upwards!");
        tree root = p.buildUp(leaves);
        System.out.println("The root of these leaves is: " + root.toString());
        
        //System.out.println("The entire tree:");
        //System.out.println(treeToStringDown(root));
        
        System.out.println("Finding alice's rep set");
        treeQueue repSet = root.findRepSet(leaves.peek(0), leaves.peek(-1), root);
        // Printing alice's rep set
        for (int i = 0; i < repSet.length; i++){
        	System.out.println(repSet.peek(i).value);
        	System.out.println("");
        }
        
        System.out.println("Generating Poly Coefficients (method one)");
        int[] coefficients = p.makeCoefficientsOne(repSet);

        System.out.println("Printing the coefficients");
        for (int i = 0; i < coefficients.length; i++){
        	System.out.println(coefficients[i]);
        }
        
        // Encrypting Coefficients
		// 128-bit encryption with 64-bit certainty (dat's a lot)
		Paillier paillier = new Paillier();
		BigInteger[] encCoe = new BigInteger[coefficients.length];
		for (int i = 0; i < coefficients.length; i++){
			encCoe[i] = paillier.Encryption(new BigInteger(String.valueOf(coefficients[i])));
		}
		
		// Make this key globally available through a singleton
		BigInteger[] tmp = paillier.privateKey();
		shareSingleton share = shareSingleton.getInstance(); 
        share.g = tmp[0];
        share.lambda = tmp[1];
        share.n = tmp[2];        
        
        System.out.println("Printing the encrypted coefficients");
        for(int i = 0; i < encCoe.length; i++){
        	System.out.println(encCoe[i]); // These are BigIntegers
        }
        
    	
    	// The format of a code 1 message:
    	// "@@1:encrypted coefficients:width:g:n"
    	String txt = "@@1";
    	for(int i = 0; i < encCoe.length; i++){ // The coefficients encrypted
    		txt += ":" + encCoe[i].toString(16);
    	}
    	
    	
    	BigInteger[] key = paillier.publicKey();	// Alice's public key
    	txt += ":" + String.valueOf(width) + ":" + key[0].toString(16) + ":" + key[1].toString(16);
    	//Log.d("sending", "the txt: " + txt);
    	
    	ArrayList<String> list = new ArrayList<String>();
    	SmsManager sms = SmsManager.getDefault();
    	list = sms.divideMessage(txt);
    	
        String number = intent.getStringExtra("number");
    	sms.sendMultipartTextMessage(number, null, list, null, null);
    	
    	/*
    	// Simple TEST //
    	Log.d("test", "beginning");
    	
    	paillier = new Paillier(32, 16);
    	
    	//paillier.loadPublicKey(paillier.g, paillier.n);
    	Log.d("test", "paillier.g: " + paillier.g);
    	Log.d("test", "paillier.lambda: " + paillier.lambda);
    	Log.d("test", "paillier.n: " + paillier.n);
    	Log.d("test", "paillier.nsquare: " + paillier.nsquare);
    	
    	BigInteger neg = paillier.Encryption(new BigInteger("-5"));
    	Log.d("test", "encryption of -5: " + neg);
    	BigInteger pos = paillier.Encryption(new BigInteger("5"));
    	Log.d("test", "encryption of 5: " + pos);
    	
    	BigInteger result = pos.multiply(neg).mod(paillier.nsquare);
    	Log.d("test", "multiplied: " + result);
    	
    	//paillier.loadPrivateKey(paillier.g, paillier.lambda, paillier.n);
    	Log.d("test", "paillier.g: " + paillier.g);
    	Log.d("test", "paillier.lambda: " + paillier.lambda);
    	Log.d("test", "paillier.n: " + paillier.n);
    	Log.d("test", "paillier.nsquare: " + paillier.nsquare);
    	
    	BigInteger clear_result = paillier.Decryption(result);
    	Log.d("test", "decrypted mult: " + clear_result);
    	
    	BigInteger clear_neg = paillier.Decryption(neg);
    	BigInteger clear_pos = paillier.Decryption(pos);
    	Log.d("test", "decrypted -5: " + clear_neg);
    	Log.d("test", "decrypted 5: " + clear_pos);
    	*/
    } // End of doItAll()
} // End of activity / class;
    	