package net.ednovak.nearby;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class Protocol {
	private final static String TAG = Protocol.class.getName();

	private static Protocol instance = null;
	private static int curState = 0;
	// Possible states =
	// 0 = Beginning (Bob is in this state when he gets a query from Alice)
	// 1 = Bob is in this state when he is waiting for Alice's various OT responses
	// 2 = Bob is in this state after he has received all of Alice's OTs and verifies that Alice is nearby

	// 0 = Beginning (Alice is in this state when she decides to send a query)
	// 21 = Alice is in this state during the various OTs
	// 22 = Alice goes to this state when the OT is done
	// 23 = Alice goes to this state when she gets Bob's location

    // Bloomfilter c = bits per element, n = number of elements, k=number of hashes
    final private BloomFilter<Integer>[] bobBF1 = new BloomFilter[]{new BloomFilter<Integer>(2, 32, 3), new BloomFilter<Integer>(2, 32, 3)};
    final private BloomFilter<Integer>[] bobBF0 = new BloomFilter[]{new BloomFilter(2, 512, 3), new BloomFilter(2, 512, 3)};

    final private BloomFilter<Integer>[] aliceBF = new BloomFilter[] {new BloomFilter(2, 32, 3), new BloomFilter(2, 32, 3)};


    private ArrayList<TreeNode>[] wallSet;
    private static Random r = new Random();
    private static int otRoundCount = 0;

    private static Context ctx;
    public static int policy;

	// Hashmap from state integer to function using ProtocolMethod interface
	HashMap<Integer, ProtocolMethod> protoCall = new HashMap<Integer, ProtocolMethod>();
	// Used to call an aribtrary method
	public interface ProtocolMethod{
		public String execute(String data);
	}

	//treeQueue leaves; // The span
	TreeLeaf user; // Location of the user (leaf node)

	// Constructor does nothing! This is probably a design flaw :P
	// Make this a singleton class
	private Protocol(Context newCtx) {
        ctx = newCtx;
        initProtoCallMap();

    }

    private void dumpFPRs(){
        // Check BF's FPR
        Log.d(TAG, "--- FPRs ---");
        for(int i = 0; i < 2; i++) {
            Log.d(TAG, TreePair.TYPE.get(i));
            Log.d(TAG, "bobBF1: (" + bobBF1[i].getBitSet().size() + " bits and " + bobBF1[i].getK() + " hashes) FPR: " + bobBF1[i].getFalsePositiveProbability());
            Log.d(TAG, "bobBF0: (" + bobBF0[i].getBitSet().size() + " bits and " + bobBF0[i].getK() + " hashes) FPR: " + bobBF0[i].getFalsePositiveProbability());
            Log.d(TAG, "aliceBF: (" + aliceBF[i].getBitSet().size() + " bits and " + aliceBF[i].getK() + " hashes) FPR: " + aliceBF[i].getFalsePositiveProbability());
            Log.d(TAG, "");
        }
    }

    private void dumpBits(){
        for(int i = 0; i < 2; i++){
            Log.d(TAG, TreePair.TYPE.get(i));
            Log.d(TAG, "bobBF1: " + bobBF1[i].getBitString());
            Log.d(TAG, "bobBF0: " + bobBF0[i].getBitString());
            Log.d(TAG, "aliceBF: " + aliceBF[i].getBitString());
        }
    }

    public void reset(){
        curState = 0;
    }

	public static Protocol getInstance(Context newCtx){
		if(instance == null){
			instance = new Protocol(newCtx);
		}
		return instance;
	}





	public String handleMsg(String data, Context ctx) {
		ProtocolMethod pm = protoCall.get(curState);
		if(pm != null) {
			return pm.execute(data);
		}

		else{ // Invalid protocol message
			Log.d(TAG, "Invalid protocol message: " + data);
			return null;
		}
	}

    private static BloomFilter<Integer> addToBF(BloomFilter<Integer> bf, ArrayList<TreeNode> nodeList){
        for(int i = 0; i < nodeList.size(); i++){
            bf.add(nodeList.get(i).getValue());
        }
        return bf;
    }

    private static BloomFilter<Integer> addPathsFromLeaves(BloomFilter<Integer> bf, TreePair tp, int TYPE){
        for(int i = 0; i < tp.leaves.get(TYPE).size(); i++){
            bf = addToBF(bf, tp.getPathSet(tp.primary.get(TYPE), 16));
        }
        return bf;
    }

    private static int getRandIndexOf(BloomFilter<Integer> bf, boolean target){
        while(true) {
            int spot = r.nextInt(bf.getBitSet().size());
            if (bf.getBit(spot) == target) {
                Log.d(TAG, "Found a " + target + " at spot: " + spot + "  proof: " + bf.getBit(spot));
                return spot;
            }
        }
    }


    private String otRound(){
        StringBuilder msg = new StringBuilder();
        for(int i = 0; i < 2; i++){
            int wallElementVal = wallSet[i].get(otRoundCount).getValue();
            int[] locations = bobBF1[i].getIndicies(wallElementVal);
            //for(int loc : locations){
                // OT for these locs and
                //
                // OT(loc, getRandIndexOf(bobBF0[i], false));

            //}

        }

		/*

        otRoundCount++;

        BloomFilter<Integer> test = new BloomFilter<Integer>(2, 8, 3);
        Log.d(TAG, "Bloom filter: " + test.getBitString());
        test.add(16);
        Log.d(TAG, "After Insertion of 16: " + test.getBitString());
        Log.d(TAG, "Hash locations of 16: " + Arrays.toString(test.getIndicies(16)));
        Log.d(TAG, "BLOOM FILTER TEST FINISHED!");
        */
        return null;
    }


	private void initProtoCallMap(){
		protoCall.put(0, new ProtocolMethod() {
			@Override
			public String execute(String data) {
                if (data.equals("ack-a")) {
                    // I am now alice and I query Bob for his location (he tells me if we're nearby)
                    curState = 21;
                    return "Where are you?";
                } else if (data.equals("Where are you?")) {
                    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
                    policy = Integer.valueOf(sharedPrefs.getString("policy", "35"));
                    NPLocation loc = NearPriLocationListener.getInstance(ctx).getLocationCopy();

                    // one lat and one lon tree
                    TreePair tp = new TreePair(loc, ctx);

					// Time to check!
					Log.d(TAG, "Lat Tree: " + tp.toDFSString(TreePair.TYPE_LAT));
					Log.d(TAG, "Lon Tree: " + tp.toDFSString(TreePair.TYPE_LON));



                    for(int i = 0; i < 2; i++){
                        wallSet[i] = tp.getWallSet(tp.root.get(i));
                        ArrayList<TreeNode> pathSet = tp.getPathSet(tp.primary.get(i), 16);
                        // Time to check again!
                        Log.d(TAG, TreePair.TYPE.get(i) + " wall set: " + wallSet);
                        Log.d(TAG, TreePair.TYPE.get(i) + " path Set: " + pathSet);

                        addToBF(bobBF1[i], wallSet[i]);
                        addPathsFromLeaves(bobBF0[i], tp, i);
                    }

                    dumpFPRs();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    return otRound();
                }
                return null;
			}
		});



	}
}
