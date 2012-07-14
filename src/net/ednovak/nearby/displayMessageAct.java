package net.ednovak.nearby;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import java.lang.Math;
import java.math.BigInteger;
import java.util.Arrays;

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
        
        // Get intent
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        
        // Create textview in java instead of XML and put the string in it
        TextView textView = new TextView(this);
        textView.setTextSize(40);
        textView.setText(message);
        
        setContentView(textView);
        
        // New instance of the protocol
        protocol p = new protocol();
        
		// Alice's policy width and x location (longitude)
        int width = p.policyToWidth(50); // These are user configurable
        int x = p.longitudeToInt(-179.9988); // just long for now
        
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
        BigInteger[] coefficients = p.makeCoefficientsOne(repSet);
        
        System.out.println("Printing the encrypted coefficients");
        for(int i = 0; i < coefficients.length; i++){
        	System.out.println(coefficients[i]);
        }
        
        
        /*
        // Testing Paillier Encryption!
		Paillier paillier = new Paillier();
		// instantiating two plaintext msgs
		
		BigInteger[] tmp = new BigInteger[repSet.length];
		for (int i = 0; i < repSet.length; i++){
			tmp[i] = new BigInteger(String.valueOf(repSet.peek(i).value));
		}
		
		for (int i = 0; i < tmp.length; i++){
			tmp[i] = paillier.Encryption(tmp[i]);
		}

		System.out.println("the encrypted and then decrypted output from the rep set values");
		for (int i = 0; i < tmp.length; i++){
			System.out.println(paillier.Decryption(tmp[i]).toString());
		}
		*/
    }
}
