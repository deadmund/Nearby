package net.ednovak.nearby;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.lang.Math;
import java.util.Arrays;

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
		return (int)Math.round((longitude / 0.00017));  
	}
	
	// No longer used! :(
	public boolean isPowerOfTwo(int n){
		return (n&(n-1)) == 0; // Checks that there is only 1 bit set to 1 
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
	
	/*
	private tree buildUp(tree[] leaves){
		tree[] outQueue = leaves;  // Nodes that we're iterating through
		tree[] inQueue; // Array that we're placing new parents into
		
		while (outQueue.length != 1)
		{
			System.out.println("Length of outQueue is still > 1.  Creating empty inQueue array");
			// Even for even nodes it might be this many because the section I'm creating might not be  
			// perfect binary tree.  (far left node has a left parent and far right has a right parent)
			inQueue = new tree[(outQueue.length/2) + 1];
		
				
			System.out.println("Filling inQueue with new parents");
			// Now to make some parents
			for(int i = 0; i < outQueue.length - 1; i = i + 2){
				char[] path = outQueue[i].path;
				// In case the path has been exhausted, this means all the remaining branches
				// will go right (or 0)
				if (path.length == '0') { 
					path = new char[1];
					path[0] = '0';
				}
					
				if (path[0] == '0'){
					double value = outQueue[i].value + 2117648;
					char[] nMap = Arrays.copyOfRange(outQueue[i].path, 1, outQueue[i].path.length - 1);
					inQueue[i/2] = new tree(i/2, value, nMap, outQueue[i], outQueue[i+1]);
				}
				else{
					double value = outQueue[i+1].value + 2117641;
					char[] nMap = Arrays.copyOfRange(path, )
					
				}
			}
			
			//System.out.println("Checking if outQueue is odd length => we need one more parent");
			if (outQueue.length % 2 == 1){
				tree lastNode = outQueue[outQueue.length - 1];
				lastNode.parent = new tree((outQueue.length/2)+1, lastNode.value+361, "tmp", lastNode, null);
				inQueue[inQueue.length -1] = lastNode.parent;
			}
			
			//System.out.println("About to make outQueue the new inQueue (pulling from outQueue next iteration");
			outQueue = inQueue; // Swap them for next iteration! :)
			//System.out.println("The outQueue is now length: " + outQueue.length);
		}
		return outQueue[0];
	}
	*/
	
	public String treeToStringDown(tree root){
		return root.toString() + treeToStringDown(root.left)+ treeToStringDown(root.right) ;
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
        int x = this.longitudeToInt(-179.9989);
        
        System.out.println("Alice's x: " + x);
        
        int left = x - width;
        int right = x + width;
        // X is on the left side of the tree
        System.out.println("left: " + left);
        System.out.println("right: " + right);
        
        // Create the leaves
        System.out.println("Creating the leaves");
        tree[] leaves = new tree[(right - left) + 1];
        int cur = left;
        for (int i = 0; i < leaves.length; i++){
        	// R is 0, L is 1
        	String mapString = new StringBuffer(Integer.toBinaryString(cur)).toString();
        	//System.out.println(cur + " => " + mapString); // char of binary string from this int ()
        	leaves[i] = new tree(i, cur, mapString.toCharArray(), null, null);
        	if (cur == x){
        		leaves[i].special = "Alice!";
        	}
        	cur += 1;
        }
        
        System.out.println("Alice's leaf nodes go from " + left + " to " + right + ".  Her node is: " + x); 
        System.out.println("Lowest possible node on the tree at long=-180: " + this.longitudeToInt(-180.0));
        System.out.println("Highest possible node on the tree at long=180: " + this.longitudeToInt(180.0));
        
        System.out.println("Here are the leaves in Alice's span:");
        for (int i = 0; i < leaves.length; i++){
        	System.out.println("" + leaves[i]);
        	System.out.println(" ");
        }
        
        
        //tree root = buildUp(leaves);
        
        //System.out.println("The root node: " + root.toString());
        
        /*
        System.out.println("The entire tree:");
        System.out.println(treeToStringDown(root));
        */
        
        
    }
}
