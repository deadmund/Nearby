package net.ednovak.nearby;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
	
	@SuppressLint("NewApi")
	private treeQueue buildLevel(treeQueue bottom, int height){
		treeQueue top = new treeQueue(); // Level above ours
	
		for (int i = 0; i < bottom.length; i++){
			tree cur = bottom.peek(i);
			
			if (cur.path.length == 0){
				cur.path = new char[1];
				cur.path[0] = '0';
			}
			
			char[] nPath; // Slice the new path
			System.out.println("cur.path.length" + cur.path.length);
			System.out.println("cur.path: " + cur.path[0] + "  cur.path.length-1: " + (cur.path.length-1));
			nPath = Arrays.copyOfRange(cur.path, 0, cur.path.length-1); // Drop the last bit
			
			//System.out.println("Checking the path letter" + old);
			
			if (cur.path[cur.path.length-1] == '0'){ // This is a branch that goes right
				int nValue = cur.value + 2117648; // Max num of leaf nodes
				System.out.println("Putting " + nValue + " in top row @ end");
				top.push(new tree(nValue, nPath, cur, null));
				cur.parent = top.peek(-1); // Thing at end
				if (i + 1 <= bottom.length -1){ // True if this node is not on the right end
					System.out.println(bottom.peek(i).value + " is not the right hand edge");
					top.peek(-1).right = bottom.peek(i+1);
					System.out.println("bottom length:" + bottom.length + " i+1:" + (i+1) + " top length:" + top.length);
					bottom.peek(i+1).parent = top.peek(-1);
					System.out.println("The problem was not above me!");
				}
				else{
					System.out.println(bottom.peek(i).value + " RIGHT END");
				}
				i = i + 1; // I just took care of the node next to me with outQueue[i+1] above
			}
			
			else { // This is a branch that goes left	
				int nValue = cur.value + (2117648 - ((height -1) * (height - 1)));
				System.out.println("Putting " + nValue + " in top row @ end");
				top.push(new tree(nValue, nPath, null, cur));
				cur.parent = top.peek(-1);
				if (i > 0){ // True if this node is not on the left end
					System.out.println(bottom.peek(i) + " is not the left hand edge");
					top.peek(-1).right = bottom.peek(i-1);
					bottom.peek(i-1).parent = top.peek(-1);
				}
				else{
					System.out.println(bottom.peek(i).value + " LEFT END");
				}
			}
		}
		return top;
	}
	
	private tree buildUp(treeQueue leaves){
		treeQueue row = leaves;  //Nodes that we're iterating through	
		int height = 1; // Currently building height 1
		
		while (row.length != 1){ // This isn't the root node yet	
			//System.out.println("Length of top > 1 so this isn't the root yet");
			row = buildLevel(row, height);
			height++;
		}
		return row.peek(0);
	}
	
	public String treeToStringDown(tree root){
		if (root == null) { return ""; }
		
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
        int width = this.policyToWidth(30);
        int x = this.longitudeToInt(-179.9988);
        
        System.out.println("Alice's x: " + x);
        
        int left = x - width;
        int right = x + width;
        // X is on the left side of the tree
        System.out.println("left: " + left);
        System.out.println("right: " + right);
        
        // Create the leaves
        System.out.println("Creating the leaves");
        treeQueue leaves = new treeQueue();
        int cur = left;
        while ( cur <= right){
        	String mapString = new StringBuffer(Integer.toBinaryString(cur)).toString(); // R is 0, L is 1;
        	//System.out.println(cur + " => " + mapString); // char of binary string from this int ()
        	leaves.push(new tree(cur, mapString.toCharArray(), null, null));
        	if (cur == x){
        		leaves.peek(-1).special = "Alice!";
        	}
        	cur += 1;
        }
        
        System.out.println("Alice's leaf nodes go from " + left + " to " + right + ".  Her node is: " + x); 
        System.out.println("Lowest possible node on the tree at long=-180: " + this.longitudeToInt(-180.0));
        System.out.println("Highest possible node on the tree at long=180: " + this.longitudeToInt(180.0));
        
        System.out.println("Here are the leaves in Alice's span:");
        for (int i = 0; i < leaves.length; i++){
        	System.out.println("" + leaves.peek(i));
        	System.out.println(" ");
        }
        
        System.out.println("Building the tree upwards!");
        tree root = buildUp(leaves);
        System.out.println("The root of these leaves is: " + root.toString());
        
        System.out.println("The entire tree:");
        System.out.println(treeToStringDown(root));
        
        
        
    }
}
