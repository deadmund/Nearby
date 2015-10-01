package net.ednovak.nearby;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by ejnovak on 9/23/15.
 */
public class TreeNode {
    private final static String TAG = TreeNode.class.getName();

    private final static int DIR_LEFT = 0;
    private final static int DIR_RIGHT = 1;

    public static boolean isLeaf = false;

    private int value;
    protected int height;
    public TreeNode left;
    public TreeNode right;
    public TreeNode parent;

    // 0 means (go right (upwards))
    // Always read from the rightmost (least significant) bit
    protected ArrayList<Integer> pathBits = new ArrayList<Integer>();
    public TreeNode(int newValue){
        value = newValue;
    }

    public int getValue() {
        return value;
    }

    public String toString(){
        String tmp = "Node: " + value + " at height: " + height;
        return tmp;
    }

    public TreeNode createParent(){
        TreeNode newParent;
        if (this.upRightward()) { // This is a branch that goes right (upward)
            int newValue = value + TreePair.magic; // Max num of leaf nodes
            newParent = new TreeNode(newValue);
            newParent.left = this;
        }

        else { // This is a branch that goes left (upward)
            int newValue = value + (TreePair.magic - (int) (Math.pow(2.0, (double) (height))));
            newParent = new TreeNode(newValue);
            newParent.right = this;
        }


        // Copy path bits (dropping last bit to give us the direction next time
        if (!noMorePathBits()) {
            ArrayList<Integer> newPathBits = deepCopyPathBits();
            //Log.d(TAG, "Removing pathBit at index: " + (pathBits.size() - 1));
            newPathBits.remove(pathBits.size()-1);
            newParent.pathBits = newPathBits;
        }

        // Link, set the height and return
        newParent.height = height + 1;
        parent = newParent;
        return newParent;

    }

    public boolean upRightward(){
        if(noMorePathBits()){
            return true;
        }
        else if(pathBits.get(pathBits.size() - 1) == 0){
            return true;
        }
        else{
            return false;
        }
    }

    private ArrayList<Integer> deepCopyPathBits(){
        ArrayList<Integer> copy = new ArrayList<Integer>();
        for(int i = 0; i < pathBits.size(); i++){
            copy.add(new Integer(pathBits.get(i)));
        }

        return copy;
    }

    private boolean noMorePathBits(){
        return (pathBits.size() == 0);
    }

    public TreeNode getLeftMostLeaf(){
        return getMostLeaf(DIR_LEFT);
    }

    public TreeNode getRightMostLeaf(){
        return getMostLeaf(DIR_RIGHT);
    }

    private TreeNode getMostLeaf(int dir){
        int tmpHeight = height;
        TreeNode cur = this;
        while(tmpHeight > 0){
            try {
                cur = cur.child(dir);
            } catch (NullPointerException e){
                return null;
            }
            tmpHeight--;
        }
        return cur;
    }

    private TreeNode child(int dir){
        if(dir == DIR_LEFT){
            return left;
        }
        else{
            return right;
        }
    }
}

