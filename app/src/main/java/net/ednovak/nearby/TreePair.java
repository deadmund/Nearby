package net.ednovak.nearby;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by ejnovak on 9/25/15.
 */
public class TreePair {
    private final static String TAG = TreePair.class.getName();

    public final static int TYPE_LAT = 0;
    public final static int TYPE_LON = 1;
    public final static HashMap<Integer, String> TYPE = new HashMap<Integer, String>(){{
        put(TYPE_LAT, "Latitude");
        put(TYPE_LON, "Longitude");
    }};

    public HashMap<Integer, TreeNode> root = new HashMap();
    public HashMap<Integer, TreeLeaf> primary = new HashMap();
    public HashMap<Integer, ArrayList<TreeLeaf>> leaves = new HashMap();

    // Where will I need the most nodes to get 10m of distance in each node?
    // For longitude it don't matter
    // For latitude, at 0deg (the equator!) where the earth is fattest
    // This is enough nodes to make 10m tiles around the equator (lat point of thickest band )
    public final static int magic = 4007500; // 4 million: 4,007,500
    //http://www.space.com/17638-how-big-is-earth.html

    Context ctx;


    // Location is a lat or lon value
    public TreePair(NPLocation loc, Context newCtx){
        ctx = newCtx;

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        int policy = Integer.valueOf(sharedPrefs.getString("policy", "35"));

        TreeLeaf latLeaf = new TreeLeaf(getLatNodeValue(loc));
        TreeLeaf lonLeaf = new TreeLeaf(getLonNodeValue(loc));

        TreeLeaf[] tmp = {latLeaf, lonLeaf};

        for(int i = 0; i < 2; i++){ // There are only two but I'm lazy, so: loop
            Log.d(TAG, " ");

            TreeLeaf prim = tmp[i];
            prim.personHere = true;
            Log.d(TAG, "prim: " + prim);
            primary.put(i, prim);

            ArrayList<TreeLeaf> tmpLeaves = new ArrayList<TreeLeaf>();

            // Make nearby leaf nodes
            int numLeaves = getNumLeafNodes(loc, i, policy);

            // Bottom half of leaves
            int newValue = prim.getValue() - 1;
            for(int j = numLeaves / 2; j > 0; j--){
                TreeLeaf newTmp = new TreeLeaf(newValue);
                tmpLeaves.add(0, newTmp);
                newValue--;
            }
            tmpLeaves.add(prim); // Middle leaf

            // Top half of leaves
            newValue = prim.getValue() + 1;
            for(int j = 0; j < numLeaves/2; j++){
                TreeLeaf newTmp = new TreeLeaf(newValue);
                tmpLeaves.add(newTmp);
                newValue++;
            }

            //Log.d(TAG, "Done making leaf nodes: " + leaves.toString());
            leaves.put(i, tmpLeaves);


            // Build Tree up
            ArrayList<TreeNode> topRow = TreeLeaf.treeLeavestoTreeNodes(tmpLeaves);
            ArrayList<TreeNode> bottomRow;

            while(topRow.size() != 1){
                // Swap rows to build another row
                bottomRow = topRow;
                topRow = new ArrayList<TreeNode>();

                for(int j = 0; j < bottomRow.size(); j++){
                    TreeNode cur = (TreeNode)bottomRow.get(j);
                    TreeNode newParent = cur.createParent();
                    topRow.add(newParent);

                    // Set the parent's children
                    if (cur.upRightward()){
                        if (j+1 < bottomRow.size()){ // We're not on the right edge and a i+1 exists
                            newParent.right = bottomRow.get(j + 1);
                            bottomRow.get(j+1).parent = newParent;
                        }
                        j = j + 1; // Get to skip a node, yay
                    }

                    else { // up leftWard
                        if (j-1 >= 0){ // We're not on the left edge and a i-1 exists
                            newParent.left = bottomRow.get(j-1);
                            bottomRow.get(j-1).parent = newParent;
                        }
                    }
                }

                //Log.d(TAG, "Bottom row: " + bottomRow.toString() + "  Top row: " + topRow.toString());
            }

            // Set the roots, although I don't think this is actually used! :P
            root.put(i, topRow.get(0));
        }

        Log.d(TAG, "Finished Constructing Tree Pair");
    }


    public ArrayList<TreeNode> getWallSet(TreeNode root){
        // Basic Idea: If a node has two children (not null)

        ArrayList<TreeNode> answer = new ArrayList<TreeNode>();
        ArrayList<TreeNode> bottom = new ArrayList<TreeNode>();
        ArrayList<TreeNode> top = new ArrayList<TreeNode>();
        top.add(root);

        while (top.size() != 0){
            for (int i = 0; i < top.size(); i++){
                TreeNode cur = top.get(i);
                // If a leaf is outside the span than it isn't in my tree and we'll see null
                if (cur.getLeftMostLeaf() == null || cur.getRightMostLeaf() == null){
                    if (cur.left != null){
                        bottom.add(cur.left);
                    }
                    if (cur.right != null){
                        bottom.add(cur.right);
                    }
                }
                else{ answer.add(cur); } // The left and right leaf was within the bounds
            }
            top = bottom;
            bottom = new ArrayList<TreeNode>();
        }
        return answer;
    }

    public ArrayList<TreeNode> getPathSet(TreeLeaf spot, int height){
        ArrayList<TreeNode> ans = new ArrayList<TreeNode>();
        TreeNode cur = spot;
        ans.add(cur);
        while(cur.height < height){
            cur = cur.createParent();
            ans.add(cur);
        }
        return ans;
    }

    public ArrayList<TreeNode> getSuperPathSet(int type, int height){
        ArrayList<TreeNode> ans = new ArrayList<TreeNode>();
        TreeLeaf[] selLeaves = new TreeLeaf[leaves.get(type).size()];
        leaves.get(type).toArray(selLeaves);

        for(int i = 0; i < selLeaves.length; i++){
            ans.addAll(getPathSet(selLeaves[i], height));
        }

        return ans;
    }



    private int getLatNodeValue(NPLocation loc){
        double totalDistance = getTotalDistance(loc, TYPE_LAT);
        NPLocation southPole = NPLocation.getSouthPole();
        double d = southPole.distanceTo(loc);
        //Log.d(TAG, "d: " + d);
        double per = d / totalDistance;
        //Log.d(TAG, "Percentage: " + per + " * " + magic + " = " + (int)(per * magic));
        //double apiDistance = loc.distanceTo(southPole);
        //Log.d(TAG, "Distance to south pole: " + d + "  api distance: " + apiDistance + "  err: " + Math.abs(d - apiDistance));
        //Log.d(TAG, "Here: " + loc.toPrettyString());
        //Log.d(TAG, "Distance south pole to here: " + d + "  distance from south pole to north pole: " + totalDistance);

        // Always use the same magic value
        // This way, Alice and Bob find themselves on the same tree, instead of
        // different trees based on locations.
        // Percentage of distance around earth (from south to north pole) * magic number (magic = total number of tree leaves)
        return (int)(per * magic);
    }

    private int getLonNodeValue(NPLocation loc){
        double totalDistance = getTotalDistance(loc, TYPE_LON);
        NPLocation IDL = NPLocation.getIDLfromLat(loc.getLatitude());
        double d = IDL.distanceTo(loc);

        // Always use the same magic value
        // This way, Alice and Bob find themselves on the same tree, instead of
        // different trees based on locations.
        // Percentage of distance around earth (around earth parallel to equator at this lat line) * magic number (magic = total number of tree leaves)
        double per = d / totalDistance;
        return (int)(per * magic);
    }

    public String toDFSString(int TYPE){
        return DFS(root.get(TYPE));
    }

    private static String DFS(TreeNode root){
        if(root == null){
            return "";
        }
        else {
            return root.toString() + " " + DFS(root.left) + " " + DFS(root.right);
        }
    }

    private int getNumLeafNodes(NPLocation loc, int type, int policy){
        double distance = getTotalDistance(loc, type);
        double nodeWidth = distance / magic;
        // Times two because we go policy meters in two directions (east and west)
        Log.d(TAG, "At: " + loc.toPrettyString() + " each node is: " + nodeWidth + "m");
        return (int)((policy / nodeWidth) * 2);
    }

    private double getTotalDistance(NPLocation loc, int type){
        if(type == TYPE_LON){

            NPLocation IDL = loc.getIDLfromLat(loc.getLatitude());

            // The IDL from the opposite side
            NPLocation IDLBack = new NPLocation();
            IDL.setLongitude(-180);
            IDL.setLatitude(loc.getLatitude());

            double totalDistance = IDL.distanceTo(IDLBack);
            return totalDistance;
        }
        else if (type == TYPE_LAT){
            // distance from south pole in meters
            NPLocation southPole = NPLocation.getSouthPole();

            NPLocation northPole = new NPLocation();
            northPole.setLongitude(0);
            northPole.setLatitude(90);

            double totalDistance = southPole.distanceTo(northPole);
            return totalDistance;
        }
        return -1.0;
    }
}
