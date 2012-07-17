package net.ednovak.nearby;

import android.util.Log;


public class protocol {
	
	treeQueue leaves; // The span
	tree user; // Location of the user (leaf node)
	
	// Constructor does nothing!  This is probably a design flaw :P
	public protocol(){
	}
	
	
	//Creates the leaf nodes from a left and right longitude value
	//X is the leaf that the user is at
	public treeQueue genLeaves(int left, int right, int x){
        System.out.println("Creating the leaves");
        treeQueue leaves = new treeQueue();
        int cur = left;
        while ( cur <= right){
        	// The map is the binary rep of this nodes value, neat huh?
        	String mapString = new StringBuffer(Integer.toBinaryString(cur)).toString(); // R is 0, L is 1;
        	//System.out.println(cur + " => " + mapString); // char of binary string from this int ()
        	leaves.push(new tree(cur, mapString.toCharArray(), null, null));
        	if (cur == x){
        		leaves.peek(-1).special = "User!";
        		user = leaves.peek(-1);
        	}
        	cur++;
        }
        return leaves;
	}
	
	/*
	// Converts longitude to the int values I use in the tree
	// This function depends on the distance in meters between degrees long
	// and the distance we want in meters between nodes
	public int longitudeToInt(double longitude){
		// This absolutely returns an int but it might be not rounded correctly...
		if (longitude < -180 || longitude > 180){
			System.out.println("Input longitude out of range: " + longitude);
			System.exit(101);
		}
		
		longitude = longitude + 180;
		// .00017 means the nodes are 10m apart and we're talking about long near the equator
		return (int)Math.round((longitude / 0.00017));   
	}
	*/
	
	
	// Converts a longitude to a leaf node value (between 0 and 2117647)
	public int longitudeToLeaf(double longitude){
		if (longitude < -180 || longitude > 180){
			System.out.println("Input longitude out of range: " + longitude);
			System.exit(101);
		}
		longitude = longitude + 180;
		// Near the equator .00017 degrees is about 10 meters along a straight line
		return (int)Math.round( (longitude / 0.00017) );
	}
	
	
	// Given a lon, lat, and distance, returns the lon at the same latitude at that distance
	public double findLong(double lon_1, double lat_1, int distance){
		Log.d("location", "lon_1: "  + lon_1 +  "   lat_1:" + lat_1);
		double d = ((double)distance / 1000); // Convert to kilometers!
		Log.d("location", "the distance set by the user in km: " + d);
		double R = 6371.0;  // Earth's radius in km
		
		
		double left = Math.cos(d / 6371.0);
		double right = Math.sin(lat_1) * Math.sin(lat_1);
		double bottom = Math.cos(lat_1) * Math.cos(lat_1);
		double lon_2 = lon_1 + Math.acos( (left - right) / bottom );
		Log.d("location", "lon_2: " + lon_2);
		return lon_2;
		
		
		//double lat_2 = Math.asin( Math.sin(lat_1) * Math.cos(d/R) + Math.cos(lat_1)*Math.sin(d/R)*Math.cos(1.57079633) );
		// 1.57079633 is 90 degrees (due East)
		//double lon_2 = lon_1 + Math.atan2(Math.sin(1.57079633) * Math.sin(d/R) * Math.cos(lat_1), Math.cos(d/R) - Math.sin(lat_1)*Math.sin(lat_2));
		//Log.d("location", "lat_2: " + lat_2);
		
		//Log.d("location", "lon_2: " + lon_2);
		//return lon_2;
		
	}
	
	
	// This function takes a tree index number (leaf) and converts it back to long value
	public double intToLongitude(int leafIndex){
		if (leafIndex < 0 || leafIndex > 2117647){
			System.out.println("Input leaf index out of range: " + leafIndex);
			System.exit(201);
		}
		// .00017 assuming the nodes are 10m apart and we're near the equator
		return ((leafIndex * .00017) + 180) - 360;
	}
	
	
	// Takes their policy and converts it to leaf node distance to create a span
	// In this case the nodes are 10m apart (because of longitudeToInt)
	public int policyToWidth(int policy){
		// Policy should be a factor of 10 but I didn't enforce that here out of laziness
		return policy / 10;
	}
	
	
	// Builds 1 level of the tree.  Starting at leaves, builds the row above the leaves
	// The following method 'buildUp' feeds this the leaves, and repeats the process up the tree
	//	@SuppressLint("NewApi")
	private treeQueue buildLevel(treeQueue bottom, int height){
		treeQueue top = new treeQueue(); // Level above ours
	
		for (int i = 0; i < bottom.length; i++){
			tree cur = bottom.peek(i);
			
			if (cur.path.length == 0){
				cur.path = new char[1];
				cur.path[0] = '0';
			}
			
			//System.out.println("cur.path.length" + cur.path.length);
			//System.out.println("cur.path: " + cur.path[0] + "  cur.path.length-1: " + (cur.path.length-1));
			char[] nPath = new char[cur.path.length-1]; // Drop the last bit (manual copy :(
			for(int j=0; j < nPath.length; j++){
				nPath[j] = cur.path[i];
			}
			
			//System.out.println("Checking the path letter" + old);
			
			if (cur.path[cur.path.length-1] == '0'){ // This is a branch that goes right
				int nValue = cur.value + 2117648; // Max num of leaf nodes
				//System.out.println("Putting " + nValue + " in top row @ end");
				top.push(new tree(nValue, nPath, cur, null));
				cur.parent = top.peek(-1); // Thing at end
				if (i + 1 <= bottom.length -1){ // True if this node is not on the right end
					//System.out.println(bottom.peek(i).value + " is not the right hand edge");
					top.peek(-1).right = bottom.peek(i+1);
					//System.out.println("bottom length:" + bottom.length + " i+1:" + (i+1) + " top length:" + top.length);
					bottom.peek(i+1).parent = top.peek(-1);
					//System.out.println("The problem was not above me!");
				}
				else{
					//System.out.println(bottom.peek(i).value + " RIGHT END");
				}
				i = i + 1; // I just took care of the node next to me with outQueue[i+1] above
			}
			
			else { // This is a branch that goes left	
				int nValue = cur.value + (2117648 - (int)(Math.pow(2.0, (double)(height -1))));
				//System.out.println("Putting " + nValue + " in top row @ end");
				top.push(new tree(nValue, nPath, null, cur));
				cur.parent = top.peek(-1);
				if (i > 0){ // True if this node is not on the left end
					//System.out.println(bottom.peek(i) + " is not the left hand edge");
					top.peek(-1).right = bottom.peek(i-1);
					bottom.peek(i-1).parent = top.peek(-1);
				}
				else{
					//System.out.println(bottom.peek(i).value + " LEFT END");
				}
			}
		}
		return top;
	}
	
	
	// Uses the previous method to build the tree upwards from the leaves
	public tree buildUp(treeQueue leaves){
		treeQueue row = leaves;  //Nodes that we're iterating through	
		int height = 1; // Currently building height 1
		
		while (row.length != 1){ // This isn't the root node yet	
			//System.out.println("Length of top > 1 so this isn't the root yet");
			row = buildLevel(row, height);
			height++;
		}
		return row.peek(0);
	}
	
	
	// Returns a string of the entire tree from 'root' downward (depth first search)
	public String treeToStringDown(tree root){
		if (root == null) { return ""; }
		
		return root.value + "\n" + treeToStringDown(root.left)+ treeToStringDown(root.right) ;
	}
	
	
	// Returns an array of coefficients that each form a poly that 
	// is rooted at a node from the given repSet
	public int[] makeCoefficientsOne(treeQueue repSet){
		int[] answer = new int[repSet.length];
		for (int i = 0; i < repSet.length; i++){
			answer[i] = repSet.peek(i).value * -1;
		}
		
		return answer;
	}
	
	
	// Convert from a hex string to regular ascii
	public String toAscii(String hex){
		if(hex.length()%2 != 0){
            System.err.println("requires EVEN number of chars");
            return null;
         }
         StringBuilder sb = new StringBuilder();               
         //Convert Hex 0232343536AB into two characters stream.
         for( int i=0; i < hex.length()-1; i+=2 ){
              /*
               * Grab the hex in pairs
               */
             String output = hex.substring(i, (i + 2));
             /*
              * Convert Hex to Decimal
              */
             int decimal = Integer.parseInt(output, 16);                 
             sb.append((char)decimal);             
         }           
         return sb.toString();
	}
	
	
	// Convert from an ascii string to hex
	public String toHex(String ascii){
		StringBuilder hex = new StringBuilder();
	       
        for (int i=0; i < ascii.length(); i++) {
            hex.append(Integer.toHexString(ascii.charAt(i)));
        }
        
        return hex.toString();
	}
}
