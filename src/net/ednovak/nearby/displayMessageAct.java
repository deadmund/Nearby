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
        
        // Protocol Stuff while the user waits
        
        // New instance of the protocol
        protocol p = new protocol();
        
		// Alice's policy width and x location (longitude)
        int width = p.policyToWidth(10); // These are user configurable
        int x = p.longitudeToInt(-179.9989); // just long for now
        
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
		// 8-bit encryption with 4-bit certainty
		Paillier paillier = new Paillier(8, 4);
		BigInteger[] encCoe = new BigInteger[coefficients.length];
		for (int i = 0; i < coefficients.length; i++){
			encCoe[i] = paillier.Encryption(new BigInteger(String.valueOf(coefficients[i])));
		}
        
        System.out.println("Printing the encrypted coefficients");
        for(int i = 0; i < encCoe.length; i++){
        	System.out.println(encCoe[i]);
        }
        
        // Things I want in the message
    	String[] values = new String[coefficients.length + 3];
    	
    	// The format of a code 1 message:
    	// "@@1:encrypted coefficients:width:g:n"
    	
    	String txt = "@@1";
    	for(int i = 0; i < coefficients.length; i++){ // The coefficients encrypted
    		txt += ":" + String.valueOf(coefficients[i]);
    	}
    	
    	BigInteger[] key = paillier.publicKey();	// Alice's public key
    	txt += ":" + String.valueOf(width) + ":" + key[0].toString() + ":" + key[1].toString();
    	//Log.d("sending", "the txt: " + txt);
    	
    	ArrayList<String> list = new ArrayList<String>();
    	SmsManager sms = SmsManager.getDefault();
    	list = sms.divideMessage(txt);
    	
    	Intent intent = getIntent();
        String number = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
    	
    	sms.sendMultipartTextMessage(number, null, list, null, null);
        
    }
}
