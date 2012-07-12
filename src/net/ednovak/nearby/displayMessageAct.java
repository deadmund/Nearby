package net.ednovak.nearby;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

@SuppressWarnings("serial")
class longitudeException extends Exception{
	public longitudeException(String msg){
		super(msg);
	}
}

public class displayMessageAct extends Activity {
	
	public int longitudeToInt(double longitude){
		// This absolutely returns an int but it might be not rounded correctly...
		if (longitude < -180 || longitude > 180){
			System.out.println("Input longitude out of range: " + longitude);
			System.exit(101);
		}
		
		longitude = longitude + 180;
		return (int)(longitude / 0.00017);  
	}
	
	public double intToLongitude(int leafIndex){
		if (leafIndex < 0 || leafIndex > 2117647){
			System.out.println("Input leaf index out of range: " + leafIndex);
			System.exit(201);
		}
		return ((leafIndex * .00017) + 180) - 360;
	}
	
	public int policyToWidth(int policy){
		// Policy should be a factor of 10 but I didn't enforce that here out of laziness
		return policy / 10;
	}

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
        
        // Alice's policy width and x location (longitude)
        int width = this.policyToWidth(20);
        int x = this.longitudeToInt(-179.9996);
        
        int left;
        int right;
        // X is on the left side of the tree
        if (x < 1058823){ 
        	left = 0;
        	right = left;
        	while (right <= x + width){
        		right += 1;
        	}
    	}
        
        // X is on the right side of the tree
    	else{
    		right = 2117647;
    		left = right;
    		while (left >= x - width){
    			left -= 1;
    		}
    	}
        
        System.out.println("line 82");
        treeNode[] leaves = new treeNode[right - left];
        int cur = left;
        for (int i = 0; i < leaves.length; i++){
        	leaves[i] = new treeNode(cur, this.intToLongitude(cur), true);
        	if (i == x){
        		leaves[i].special = "Alice!";
        	}
        	cur += 1;
        }
        
        System.out.println("line 86");
        
        System.out.println("Alice's leaf nodes go from " + left + " to " + right + ".  Her node is: " + x); 
        System.out.println("Lowest possible node at long=-180: " + this.longitudeToInt(-180.0));
        System.out.println("Highest possible node at long=180: " + this.longitudeToInt(180.0));
        
        System.out.println("Here are the leaves:");
        for (int i = 0; i < leaves.length; i++){
        	System.out.println(" " + leaves[i]);
        }
    }
}
