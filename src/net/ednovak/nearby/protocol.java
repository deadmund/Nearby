package net.ednovak.nearby;

import java.math.BigInteger;
import java.util.Random;

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
	
	public int latitudeToLeaf(double latitude){
		if (latitude < -90 || latitude > 90){
			System.out.println("Input latitude out of range: "  + latitude);
			System.exit(102);
		}
		
		latitude = latitude + 90;
		// On a line that goes through the poles .00000899 degrees is about 10 meters.
		// This (like the longitude) means the protocol granularity is at best 10 meters
		// I am assuming the Earth is a perfect sphere with radius 6371.  Even though
		// it is not.
		return (int)Math.round( (latitude / 0.000044966242717) );		
	}
	
	
	// Converts a longitude to a leaf node value (between 0 and 2117647)
	public int longitudeToLeaf(double longitude){
		if (longitude < -180 || longitude > 180){
			System.out.println("Input longitude out of range: " + longitude);
			System.exit(101);
		}
		longitude = longitude + 180;
		// Along the equator 10 meters is about .00000899 degrees.  This
		// (like latitude) means that the protocol granularity is at best 10 meters
		// I am assuming the Earth is a perfect sphere with radius 6371.  Even though
		// it is not.
		return (int)Math.round( (longitude / 0.000089932161921) );
	}
	
	public double findLat(double orig_lon_1, double orig_lat_1, int distance){
		//Log.d("location", "coming in");
		//Log.d("location", "orig_lon_1: " + orig_lon_1);
		//Log.d("location", "orig_lat_1: " + orig_lat_1);
		//Log.d("location", "distance: " + distance);
		
		double d = (double)distance / 1000;
		double dist = d / 6371.0;
		double brng = 0 * (Math.PI/180);
		double lat1 = orig_lat_1 * (Math.PI/180);
		//double lon1 = orig_lon_1 * (Math.PI/180);
		
		double lat2 = Math.asin( Math.sin(lat1)*Math.cos(dist) + Math.cos(lat1)*Math.sin(dist)*Math.cos(brng) );
		lat2 = lat2 * (180 / Math.PI);
		//Log.d("location", "lat2degrees: " + lat2);
		return lat2;
	}
	
	
	// Given a lon, lat, and distance, returns the lon at the same latitude at that distance
	public double findLong(double orig_lon_1, double orig_lat_1, int distance){
		//Log.d("location", "coming in");
		//Log.d("location", "orig_lon_1: " + orig_lon_1);
		//Log.d("location", "orig_lat_1: " + orig_lat_1);
		//Log.d("location", "distance: " + distance);
		
		double d = (double)distance / 1000;
		double dist = d / 6371.0;
		double brng = 90 * (Math.PI/180);
		double lat1 = orig_lat_1 * (Math.PI/180);
		double lon1 = orig_lon_1 * (Math.PI/180);
		
		double lat2 = Math.asin( Math.sin(lat1)*Math.cos(dist) + Math.cos(lat1)*Math.sin(dist)*Math.cos(brng) );
		//Log.d("location", "lat2: " + lat2);
		
		//double lat2deg = lat2 * (180 / Math.PI);
		//Log.d("location", "lat2 in degrees: " + lat2deg);
		
		double lon2 = lon1 + Math.atan2( Math.sin(brng)*Math.sin(dist)*Math.cos(lat1), Math.cos(dist)-Math.sin(lat1)*Math.sin(lat2) );
		
		//Log.d("location", "lon2 before normalizing: " + lon2);
		lon2 = (lon2+3*Math.PI) % (2*Math.PI) - Math.PI; // Apparently normalizes it to with [-180,180] ?
		
		//Log.d("location", "lon2 before radians->degrees: " + lon2);
		lon2 = lon2 * (180 / Math.PI); // Convert from radians to degrees
		
		Log.d("location", "lon2: " + lon2);
		return lon2;
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
				nPath[j] = cur.path[j];
			}
			
			//System.out.println("Checking the path letter" + old);
			
			if (cur.path[cur.path.length-1] == '0'){ // This is a branch that goes right
				int nValue = cur.value + 4030175; // Max num of leaf nodes
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
				int nValue = cur.value + (4030175 - (int)(Math.pow(2.0, (double)(height -1))));
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
	
	

	
	// Multiplys two polynomials.  We define a polynomial as a reverse string of 
	// integers.  For example poly a = x^2 - 4x + 5 int[] a = {5, -4, 1}
	// For any i > 2; a[i] = 0;
	private int[] multPolys(int[]a, int[]b){
		
		/*
		// Print A and B
		for (int i = 0; i < a.length; i++){
			Log.d("poly", "a[" + i +"]: " + a[i]);
		}
		for (int i = 0; i < b.length; i++){
			Log.d("poly", "b[" + i +"]: " + b[i]);
		}
		*/
		
		// Initialize C
		int cLength = (a.length -1) + (b.length - 1) + 1;
		int[] c = new int[cLength];
		//Log.d("poly", "c.length: " + c.length);
		
		// Pad a & b
		int[] newA = new int[cLength];
		for (int i = 0; i < a.length; i++){
			newA[i] = a[i];
		}
		int[] newB = new int[cLength];
		for (int i = 0; i < b.length; i++){
			newB[i] = b[i];
		}
		
		/*
		// Print a & b padded
		for (int i = 0; i < newA.length; i++){
			Log.d("poly", "newa[" + i +"]: " + newA[i]);
		}
		for (int i = 0; i < newB.length; i++){
			Log.d("poly", "newB[" + i +"]: " + newB[i]);
		}
		*/
		
		// Generate C
		for (int n = 0; n < c.length; n++){
			int tmp = 0;
			//Log.d("poly", "Generating c[" + n + "]");
			for( int k = 0; k <= n; k++){
				//Log.d("poly", "k: " + k + "    n:" + n);
				tmp += newA[k] * newB[n-k];
			}
			c[n] = tmp;
			//Log.d("poly", "c[" + n + "]: " + c[n]);
		}
		
		/*
		// Print C
		for (int i = 0; i < c.length; i++){
			Log.d("poly", "c[" + i +"]: " + c[i]);
		}
		*/
		return c;
		
	}
	
	private int[][] genPolysFromRoots(treeQueue trees){
		// Generate Result
		int[][] result = new int[trees.length][2];
		for (int i = 0; i < trees.length; i++){
			int[] tmp = new int[2];
			tmp[0] = trees.peek(i).value * -1;
			tmp[1] = 1;
			result[i] = tmp;
			//Log.d("poly", "result[" + i + "]: " + result[i][0] + " " + result[i][1]);
		}
		
		// Print Result
		/*
		for(int i = 0; i < result.length; i++){
			for (int j = 0; j < result[i].length; j++){
				Log.d("poly", "result[" + i +"][" + j + "]: " + result[i][j]);
			}
		}
		*/
		return result;
	}
	
	
	// Generates one large polynomial rooted at all the points in the rep set
	public int[] makeCoefficientsTwo(treeQueue repSet){
		
		int[][] polys = genPolysFromRoots(repSet);
		int[] cur = multPolys(polys[0], polys[1]);
		for (int i = 2; i < polys.length; i++){
			cur = multPolys(cur, polys[i]);
		}
		return cur;
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
	
	
	public int[] makeCoefficients(treeQueue repSet){
		// Get the setting
		
		if ( ){
			// This returns the coefficients of several polynomials
			// They are of the form (x - coe).  The 1 in front of the x is implied
			return makeCoefficientsOne(repSet);
		}
		
		else ( ) {
			// This returns coefficients of one polynomial
			// It is of the form (c_nx^n + c_n-1 * x^n-1 ...)
			return makeCoefficientsTwo(repSet);
		}
		}
	}

	
	// Does Bob's calculations 
	// (assumes we received many polys but I think it doen't matter?, IDK, gotta think 'bout that)
	public BigInteger[] bobCalc(treeQueue coveringSet, String[] encCoe){
		BigInteger[] results = new BigInteger[(encCoe.length - 5) * coveringSet.length];
        Random randomGen = new Random();
        
        Paillier paillierE = new Paillier();
        paillierE.loadPublicKey(new BigInteger(encCoe[encCoe.length - 2]), new BigInteger(encCoe[encCoe.length-1]));
        
        // This should probs be a protocol function
        // Evaluate the polys
    	for (int j = 0; j < coveringSet.length; j++){
    		int tmp = coveringSet.peek(j).value;
    		BigInteger bob = new BigInteger(String.valueOf(tmp));
    		bob = paillierE.Encryption(bob);
    		for (int i = 2; i < encCoe.length - 3; i++){ // The last token is the width
    			BigInteger alice = new BigInteger(encCoe[i]);
    			BigInteger c = bob.multiply(alice).mod(paillierE.nsquare);
    			
    			// Pack them randomly to send back
    			boolean unplaced = true;
    			while (unplaced){
    				int randInt = randomGen.nextInt(results.length);
    				//Log.d("receive", "trying spot: " + randInt);
    				if ( results[randInt] == null){
    					results[randInt] = c;
    					unplaced = false;
    				}
    			}	
    		}
        }
    	return results;
	}
}

