package net.ednovak.nearby;

import java.math.BigInteger;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
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
        
        // Get stuff from intent
        Intent intent = getIntent();
        double lon = intent.getDoubleExtra("lon", 0.0);
        double lat = intent.getDoubleExtra("lat", 0.0);
        int policy = intent.getIntExtra("policy", 0);
        String number = intent.getStringExtra("number");
        
        // Gotta get this stuff from the intent
        doItAll(lon, lat, policy, number, 1); // Do everything (this is stage 1, Alice sending to Bob)

    } // End of onCreate
    
    private void doItAll(double lon, double lat, int pol, String number, int stage){
    	Log.d("stage 1", "Alice finding / sending lon");
    	
    	// Store the policy, latitude, and longitude
    	// This shareSingleton is also used to store the private key 
    	// until the end of the protocol.
		shareSingleton share = shareSingleton.getInstance();
		share.pol = pol;
		share.lon = lon;
		share.lat = lat;
		share.number = number;
    	
        // New instance of the protocol
        protocol p = new protocol();
        
        // Probs not needed anymore!!!
		// Alice's policy width and x location (longitude)
        //int width = p.policyToWidth(pol); // These are user configurable
        
        double edge = p.findLong(lon, lat, pol);
        int edgeLeafNumber = p.longitudeToLeaf(edge);
        int aliceLeafNumber = p.longitudeToLeaf(lon);
        
        Log.d("stage 1", "Alice's leaf value: " + aliceLeafNumber);
        Log.d("stage 1", "Edge gps lon value: " + edge);
        Log.d("stage 1", "edge leaf value: " + edgeLeafNumber);
        
        int spanLength = ( Math.abs(edgeLeafNumber - aliceLeafNumber) * 2 ) + 1;
        int left = aliceLeafNumber - (spanLength / 2);
        int right = aliceLeafNumber + (spanLength / 2);
        
        Log.d("stage 1", "Alice's leaf nodes go from " + left + " to " + right + ".  Her node is: " + aliceLeafNumber);
        Log.d("stage 1", "Lowest possible node on the tree at long=-180: " + p.longitudeToLeaf(-180.0));
        Log.d("stage 1", "Highest possible node on the tree at long=180: " + p.longitudeToLeaf(180.0));
        
        treeQueue leaves = p.genLeaves(left, right, aliceLeafNumber);
        
        Log.d("stage 1", "Here are the leaves in Alice's span:");
        for (int i = 0; i < leaves.length; i++){
        	System.out.println("" + leaves.peek(i));
        	System.out.println(" ");
        }
        
        System.out.println("Building the tree upwards!");
        tree root = p.buildUp(leaves);
        System.out.println("The root of these leaves is: " + root.toString());
        
        Log.d("stage 1", "The entire tree:");
        Log.d("stage 1", "" + p.treeToStringDown(root));
        
        Log.d("stage 1", "Finding alice's rep set");
        treeQueue repSet = root.findRepSet(leaves.peek(0), leaves.peek(-1), root);
        // Printing alice's rep set
        for (int i = 0; i < repSet.length; i++){
        	System.out.println(repSet.peek(i).value);
        	System.out.println("");
        }
        
        Log.d("stage 1", "Generating Poly Coefficients (method two)");
        
        //int[] coefficients = p.makeCoefficientsOne(repSet);
        int[] coefficients = p.makeCoefficientsTwo(repSet);

        System.out.println("Printing the coefficients");
        for (int i = 0; i < coefficients.length; i++){
        	System.out.println(coefficients[i]);
        }
        
        // Encrypting Coefficients
		Paillier paillier = new Paillier();
		BigInteger[] encCoe = new BigInteger[coefficients.length];
		for (int i = 0; i < coefficients.length; i++){
			encCoe[i] = paillier.Encryption(new BigInteger(String.valueOf(coefficients[i])));
		}
		
		// Make this key globally available through the shareSingleton 'share'
		BigInteger[] tmp = paillier.privateKey();
        share.g = tmp[0];
        share.lambda = tmp[1];
        share.n = tmp[2];        
        
        System.out.println("Printing the encrypted coefficients");
        for(int i = 0; i < encCoe.length; i++){
        	System.out.println(encCoe[i]); // These are BigIntegers
        }
        
    	
        // Generate the message to send to Bob for stage 2
    	// The format of a code 1 message:
    	// "@@1:encrypted coefficients:width:g:n"
    	String txt = "@@1";
    	for(int i = 0; i < encCoe.length; i++){ // The coefficients encrypted
    		txt += ":" + encCoe[i].toString(16);
    	}
    	
    	
    	BigInteger[] key = paillier.publicKey();	// Alice's public key
    	txt += ":" + String.valueOf(spanLength / 2) + ":" + key[0].toString(16) + ":" + key[1].toString(16);
    	//Log.d("sending", "the txt: " + txt);
    	
    	ArrayList<String> list = new ArrayList<String>();
    	SmsManager sms = SmsManager.getDefault();
    	list = sms.divideMessage(txt);
    	Log.d("stage 1", "sending the encrypted coefficients (and other stuff) to Bob");
    	sms.sendMultipartTextMessage(number, null, list, null, null);
    	
    } // End of doItAll()
} // End of activity / class;
    	