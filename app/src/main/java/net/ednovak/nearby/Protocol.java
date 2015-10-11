package net.ednovak.nearby;

import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.NetworkRequest;
import android.preference.PreferenceManager;
import android.util.Log;

import net.yishanhe.mobilesc.rsaOT.BasePrimeOTR;
import net.yishanhe.mobilesc.rsaOT.BasePrimeOTS;

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
    private final static int halfSize = 2048;
    private final static int k = 3;
    private final static int N = 2;

    // Bloomfilter c = bits per element, n = number of elements, k=number of hashes
    final private BloomFilter<Integer>[] bobBF1 = new BloomFilter[]{new BloomFilter(2, halfSize, k), new BloomFilter(2, halfSize, k)};
    final private BloomFilter<Integer>[] bobBF0 = new BloomFilter[]{new BloomFilter(2, halfSize, k), new BloomFilter(2, halfSize, k)};
    final private BloomFilter<Integer>[] aliceBF = new BloomFilter[] {new BloomFilter(2, halfSize, k), new BloomFilter(2, halfSize, k)};


    private ArrayList<TreeNode>[] wallSet = new ArrayList[2];
    private ArrayList<TreeNode>[] pathSet = new ArrayList[2];
    private ArrayList<TreeNode>[] superPathSet = new ArrayList[2];

    private BasePrimeOTR bob; // Bob
    private BasePrimeOTS alice; // Alice

    private byte[][][] toSend; // The data that Alice will send to Bob (reps her BF).
    private BigInteger[] bobCS;

    private static Random r = new Random();
    private static SecureRandom secR = new SecureRandom();
    private OTSelection selection;
    public static int policy;


    private DSAPublicKey alicePub;
    private MessageDigest aliceMd;
    private DSAPrivateKey alicePriv;

    private DSAPublicKey bobPub; // Bob's copy of hte public key from alice

    private static OutputStream sockOut;

    private static Context ctx;


	// Hashmap from state integer to function using ProtocolMethod interface
	HashMap<Integer, ProtocolMethod> protoCall = new HashMap<Integer, ProtocolMethod>();
	// Used to call an aribtrary method
	public interface ProtocolMethod{
		public byte[] execute(byte[] data);
	}

	//treeQueue leaves; // The span
	TreeLeaf user; // Location of the user (leaf node)

	// Constructor does nothing! This is probably a design flaw :P
	// Make this a singleton class
	private Protocol(Context newCtx) {
        ctx = newCtx;
        initProtoCallMap();

    }

    private void dumpBFFPRs(){
        // Check BF's FPR
        Log.d(TAG, "--- FPRs ---");
        for(int i = 0; i < 2; i++) {
            Log.d(TAG, TreePair.TYPE.get(i));
            Log.d(TAG, "bobBF1: (" + bobBF1[i].size() + " bits and " + bobBF1[i].getK() + " hashes) FPR: " + bobBF1[i].getFalsePositiveProbability());
            Log.d(TAG, "bobBF0: (" + bobBF0[i].size() + " bits and " + bobBF0[i].getK() + " hashes) FPR: " + bobBF0[i].getFalsePositiveProbability());
            Log.d(TAG, "aliceBF: (" + aliceBF[i].size() + " bits and " + aliceBF[i].getK() + " hashes) FPR: " + aliceBF[i].getFalsePositiveProbability());
            Log.d(TAG, "");
        }
    }

    private void dumpBFBits(){
        Log.d(TAG, "--- Bloom Filter Bits ---");
        for(int i = 0; i < 2; i++){
            Log.d(TAG, TreePair.TYPE.get(i));
            Log.d(TAG, "bobBF1: \t\t" + bobBF1[i].getBitString());
            Log.d(TAG, "bobBF0: \t\t" + bobBF0[i].getBitString());
            Log.d(TAG, "aliceBF: \t\t" + aliceBF[i].getBitString());
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


	public byte[] handleMsg(byte[] data, Context ctx) {
		ProtocolMethod pm = protoCall.get(curState);
		if(pm != null) {
			return pm.execute(data);
		}

		else{ // Invalid state!
            return invalid("No function for state: " + curState);
		}
	}

    private static BloomFilter<Integer> addToBF(BloomFilter<Integer> bf, ArrayList<TreeNode> nodeList){
        for(int i = 0; i < nodeList.size(); i++){
            bf.add(nodeList.get(i).getValue());
        }
        return bf;
    }

    private static int getRandIndexOf(BloomFilter<Integer> bf, boolean target){
        while(true) {
            int spot = r.nextInt(bf.size());
            if (bf.getBit(spot) == target) {
                //Log.d(TAG, "Found a " + target + " at spot: " + spot + "  proof: " + bf.getBit(spot));
                return spot;
            }
        }
    }


	private void initProtoCallMap(){
		protoCall.put(0, new ProtocolMethod() {
            @Override
            public byte[] execute(byte[] input) {
                String data = new String(input, Charset.forName("UTF-8"));
                data = data.trim();
                if (data.equals("ack-a")) { // Then this is alice
                    // I am now alice and I query Bob for his location (he tells me if we're nearby)

                    NearPriLocationListener NPll = NearPriLocationListener.getInstance(ctx);
                    NPLocation loc = NPll.getLocationCopy();

                    TreePair tp = new TreePair(loc, ctx);
                    for(int i = 0; i < 2; i++){
                        pathSet[i] = tp.getPathSet(tp.primary.get(i), 16);
                        for(int j = 0; j < pathSet[i].size(); j++){
                            aliceBF[i].add(pathSet[i].get(j).getValue());
                        }
                    }

                    KeyPairGenerator keyPairG = null;
                    try{
                        keyPairG = KeyPairGenerator.getInstance("DSA");
                    } catch (NoSuchAlgorithmException e){
                        e.printStackTrace();
                    }
                    if(keyPairG != null){
                        keyPairG.initialize(1024, secR);
                    } else {
                        Log.d(TAG, "Key generation failed!");
                        return null;
                    }
                    KeyPair keyPair = keyPairG.generateKeyPair();
                    alicePub = (DSAPublicKey)keyPair.getPublic();
                    alicePriv = (DSAPrivateKey)keyPair.getPrivate();

                    BigInteger p = alicePub.getParams().getP();
                    BigInteger q = alicePub.getParams().getG();
                    BigInteger g = alicePub.getParams().getG();

                    MessageDigest md = null;
                    try{
                        md = MessageDigest.getInstance("SHA1");
                    } catch (NoSuchAlgorithmException e){
                        e.printStackTrace();
                    }
                    aliceMd = md;

                    String send = p.toString() + "," + q.toString() + "," + g.toString() + "," + N;
                    Log.d(TAG, "Sending: " + send);
                    curState = 101;
                    return send.getBytes();

                } else { // This is bob
                    // Data has already been convered to string and trim'd above
                    // Bob should have just gotten p, q, and g from Alice
                    String[] bits = data.split(",");

                    BigInteger p = new BigInteger(bits[0]);
                    BigInteger q = new BigInteger(bits[1]);
                    BigInteger g = new BigInteger(bits[2]);
                    int N = Integer.valueOf(bits[3]);

                    NearPriLib.dump("p, q, g, and N from Alice: ", bits, ",");
                    MessageDigest md = null;
                    try{
                        md = MessageDigest.getInstance("SHA1");
                    } catch (NoSuchAlgorithmException e){
                        e.printStackTrace();
                    }

                    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
                    policy = Integer.valueOf(sharedPrefs.getString("policy", "35"));
                    NPLocation loc = NearPriLocationListener.getInstance(ctx).getLocationCopy();

                    // one lat and one lon tree
                    TreePair tp = new TreePair(loc, ctx);

                    // Time to check!
                    Log.d(TAG, "Lat Tree: " + tp.toDFSString(TreePair.TYPE_LAT));
                    Log.d(TAG, "Lon Tree: " + tp.toDFSString(TreePair.TYPE_LON));


                    for (int i = 0; i < 2; i++) {
                        wallSet[i] = tp.getWallSet(tp.root.get(i));
                        superPathSet[i] = tp.getSuperPathSet(i, 16);

                        // Time to check again!
                        //Log.d(TAG, TreePair.TYPE.get(i) + " wall set: " + wallSet);
                        //Log.d(TAG, TreePair.TYPE.get(i) + " superPath Set: " + superPathSet);

                        addToBF(bobBF1[i], wallSet[i]);
                        addToBF(bobBF0[i], superPathSet[i]);
                    }
                    Log.d(TAG, "Latitude wallSet size: " + wallSet[0].size());



                    dumpBFFPRs();

                    // Create selection and subset indicies
                    int l = wallSet[0].size();
                    selection = new OTSelection(2 * k * l);
                    selection.flipHalf();
                    // There are k hashes and the length of bob's wall set is l
                    // Therefore, there are 2 * k * l real spots being requested
                    // k*l spots that have a 1 value and k*l spots that have a 0 value
                    // Then an additional 2 * k * l dummy spots.  Half of the real spots
                    // will go on the left side of the table (selection bit = 1) and the other
                    // half (2 * k * l) will go on the right side (selection bit = 0)
                    // Therefore there are 4 * k * l spots requested in total
                    // -----------------------
                    // |spots / data |  sel  |
                    // -----------------------
                    // | BF1_1, fake |   1   |
                    // | BF1_2, fake |   1   |
                    // | fake, BF1_3 |   0   |
                    // | fake, BF1_4 |   0   |
                    // | BF0_1, fake |   1   |
                    // | fake, BF0_2 |   0   |
                    // | fake, BF0_3 |   0   |
                    // | BF0_4, fake |   1   |
                    // -----------------------
                    //
                    // Also, my table doesn't show it, but the order of the BFX_Y's should
                    // be random as well

                    ArrayList<BFSpot> reals = new ArrayList<BFSpot>();
                    // Add the value 1 spots (from bobBF1)
                    for (int i = 0; i < l; i++) { // Because I have a nested function, I do only l here
                        TreeNode cur = wallSet[0].get(i);
                        int[] indices = bobBF1[0].getIndices(cur.getValue());
                        NearPriLib.dump("Indices of node " + i + " (" + cur.getValue() + "): ", indices, ",");
                        addSpots(reals, indices, 1, TreePair.TYPE_LAT, i);
                    }

                    // Add the value 0 spots (from bobBF0)
                    for (int i = 0; i < k*l; i++){
                        int index = getRandIndexOf(bobBF0[0], false);
                        reals.add(new BFSpot(index, 0, TreePair.TYPE_LAT));
                    }

                    // Shuffle the reals
                    Collections.shuffle(reals);


                    // Create answers table, spots list (of indices for Alice to use) at same time
                    int curRealIndex = 0;
                    ArrayList<BFSpot> spots = new ArrayList<BFSpot>();
                    for(int i = 0; i < selection.size(); i++){
                        if(selection.get(i) == 1){ // Put the real value in first (may be 0 or 1)
                            spots.add(reals.get(curRealIndex));
                            curRealIndex++;

                            spots.add(new BFSpot(bobBF1[0].randomIndex(), BFSpot.TYPE_FAKE, BFSpot.TYPE_FAKE));
                        }
                        else{ // Put the fake value in first (so it's on the left side of the table)
                            spots.add(new BFSpot(bobBF1[0].randomIndex(), BFSpot.TYPE_FAKE, BFSpot.TYPE_FAKE));

                            spots.add(reals.get(curRealIndex));
                            curRealIndex++;
                        }
                    }

                    Log.d(TAG, "Finished making selection vector: " + selection.toString());
                    NearPriLib.dump("Vector of spots: ", spots, "; ");

                    //dumpBFBits();

                    Log.d(TAG, "Selection: \t" + selection.toString());

                    bob = new BasePrimeOTR(p, q, g, selection.toIntArray(), md, N);

                    curState = 201;
                    String subSetVector = NearPriLib.prettyArray(NearPriLib.bfSpotsToIntArray(spots), ',');
                    return subSetVector.getBytes();
                }
            }
        });


        protoCall.put(101, new ProtocolMethod() {
            @Override
            public byte[] execute(byte[] input) {
                String data = new String(input, Charset.forName("UTF-8"));
                data = data.trim();

                String[] subSetVector = data.split(",");

                NearPriLib.dump("subSetVector: ", subSetVector, ",");

                //dumpBFBits();
                dumpBFFPRs();

                String[] subSet = new String[subSetVector.length];

                for(int i = 0; i < subSetVector.length; i++){
                    if(aliceBF[0].getBit(Integer.valueOf(subSetVector[i]))){
                        subSet[i] = "1";
                    } else {
                        subSet[i] = "0";
                    }
                }

                for(int i = 0; i < aliceBF[0].size(); i++){
                    if(aliceBF[0].getBit(i)) {
                        Log.d(TAG, "AliceBF = 1 at: " + i);
                    }
                }

                NearPriLib.dump("Subset values: ", subSet, ",");
                toSend = new byte[subSet.length/2][2][1];
                int j = 0;
                for(int i = 0; i < toSend.length; i++){
                    toSend[i][1][0] = Byte.valueOf(subSet[j]);
                    toSend[i][0][0] = Byte.valueOf(subSet[j+1]);
                    j = j + 2;
                }

                Log.d(TAG, "toSend: " + NearPriLib.prettyArray(toSend, ',', ';', '|'));

                DSAParams par = alicePub.getParams();
                alice = new BasePrimeOTS(par.getP(), par.getG(), par.getG(), aliceMd, toSend.length, N, toSend);
                BigInteger[] cs = alice.getCs();
                Log.d(TAG, "Created " + cs.length + " cs");
                //NearPriLib.dump("CS: ", cs, ",");

                String csStr = NearPriLib.prettyArray(cs, ',');

                curState = 102;
                return csStr.getBytes();
            }
        });

        protoCall.put(201, new ProtocolMethod() {
            @Override
            public byte[] execute(byte[] input) {
                String data = new String(input, Charset.forName("UTF-8"));
                data = data.trim();
                //Log.d(TAG, "I just got this: " + data + " and I am Bob!");

                String[] args = data.split(",");
                bobCS = new BigInteger[args.length];
                for(int i = 0; i < args.length; i++){
                    bobCS[i] = new BigInteger(args[i]);
                }

                BigInteger[] pk0s = bob.preparePK0(bobCS);
                String pk0sStr = NearPriLib.prettyArray(pk0s, ',');
                Log.d(TAG, "Send: " + pk0s.length + " pk0s, length in bytes: " + pk0sStr.getBytes().length);

                curState = 202;
                return pk0sStr.getBytes();
            }
        });

        protoCall.put(102, new ProtocolMethod() {
            @Override
            public byte[] execute(byte[] input) {
                String data = new String(input, Charset.forName("UTF-8"));
                data = data.trim();
                //Log.d(TAG, "PK0's: " + data);

                String[] args = data.split(",");
                BigInteger[] pk0s = new BigInteger[args.length];
                for(int i = 0; i < args.length; i++){
                    pk0s[i] = new BigInteger(args[i]);
                }

                String pk0sStr = NearPriLib.prettyArray(pk0s, ',');
                Log.d(TAG, "Got: " + pk0s.length + " pk0s, length in bytes: " + pk0sStr.getBytes().length);

                byte[][][] encrypted = new byte[toSend.length][2][1];
                //Log.d(TAG, "Encrypted Bytes (should be blank): " + NearPriLib.prettyArray(encrypted, ',', ';', '|'));
                alice.onReceivePK0s(pk0s, encrypted);
                Log.d(TAG, "Encrypted Bytes (should have selection data): " + NearPriLib.prettyArray(encrypted, ',', ';', '|'));


                // Flatten encryption into a string
                StringBuilder sb = new StringBuilder();
                for(int i = 0; i < encrypted.length; i++){
                    for(int j = 0; j < encrypted[i].length; j++){
                        sb.append(encrypted[i][j][0] + ",");
                    }
                    sb.append(";");
                }
                sb.deleteCharAt(sb.length()-1); // Delete last ";"
                Log.d(TAG, "Encryption: " + sb.toString());

                curState = 103;
                return sb.toString().getBytes();
            }
        });

        protoCall.put(202, new ProtocolMethod() {
            @Override
            public byte[] execute(byte[] input) {
                String data = new String(input, Charset.forName("UTF-8"));
                data = data.trim();
                String[] rows = data.split(";");
                byte[][][] encrypted = new byte[rows.length][2][1];
                for(int i = 0; i < encrypted.length; i++){
                    String p1 = rows[i].split(",")[0];
                    String p2 = rows[i].split(",")[1];
                    encrypted[i][0][0] = Byte.valueOf(p1);
                    encrypted[i][1][0] = Byte.valueOf(p2);
                }

                byte[][] result = bob.onReceiveEncByte(encrypted, bobCS);
                byte[][][] allresult = bob.tryDecAll(encrypted, bobCS);

                Log.d(TAG, "result: " + NearPriLib.prettyArray(result, ',', ';'));
                Log.d(TAG, "All Result: " + NearPriLib.prettyArray(allresult, ',', ';', '|'));

                // Check
                // Really bad and hacky solution!  Alice can just guarantee she sends k 1's and k 0's
                // I should be checking that spots x, y, and z are one and so on.
                if(count(result, new Byte("1")) >= k && count(result, new Byte("0")) >= k){
                    Log.d(TAG, "SUCCESS!");
                }

                curState = 0;
                reset();
                return null;
            }
        });
    }



    private byte[] invalid(String data){
        Log.d(TAG, "Protocol broken.  Most recent message data as String: " + data);
        return null;
    }


    private static void addSpots(ArrayList<BFSpot> spots, int[] indices, int spotVal, int TREE_TYPE, int setNumber) {
        for (int j = 0; j < indices.length; j++) {
            int index = indices[j];
            spots.add(new BFSpot(index, spotVal, TREE_TYPE));
        }
    }

    private static int count(byte[][] input, byte val){
        int count = 0;
        for(int i = 0; i < input.length; i++){
            if(input[i][0] == val){
                count++;
            }
        }
        return count;
    }
}
