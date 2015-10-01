package net.ednovak.nearby;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by ejnovak on 9/24/15.
 */
public class TreeLeaf extends TreeNode {
    private final static String TAG = TreeLeaf.class.getName();

    public boolean personHere = false;
    public static boolean isLeaf = true;

    public TreeLeaf(int value){
        super(value);
        super.pathBits = TreeLeaf.getBitArray(value);
        //Log.d(TAG, "Created leaf node at value: " + value + "  pathBits: " + pathBits + "  and super.pathBits is: " + super.pathBits);
        super.height = 0;
    }


    private static ArrayList<Integer> getBitArray(int value){
        int size = (int) (Math.floor(Math.log(value) / Math.log(2)) + 1);
        ArrayList<Integer> bits = new ArrayList<Integer>();
        int curValue = value;
        for(int i = 0; i < size; i++){
            bits.add(0, curValue % 2);
            curValue = curValue / 2;
        }
        return bits;
    }

    public String toString(){
        String tmp = super.toString();
        if(personHere){
            tmp = tmp + " Person Here!";
        }
        return tmp;
    }

    public static ArrayList<TreeNode> treeLeavestoTreeNodes(ArrayList<TreeLeaf> input){
        ArrayList<TreeNode> output = new ArrayList<TreeNode>();
        for(int i = 0; i < input.size(); i++){
            output.add(input.get(i));
        }
        return output;
    }
}
