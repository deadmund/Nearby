package net.ednovak.nearby;

/**
 * Created by ejnovak on 10/5/15.
 */
public class BFSpot {
    public int index;       // The index of the spot in the BF
    public int type;        // is this a one spot or a zero spot?
    public int treeType;    // type of the tree (lat or lon)

    public final static int TYPE_FAKE = -2;

    public BFSpot(int newIndex, int newType, int newTreeType){
        index = newIndex;
        treeType = newTreeType;
        type = newType;
    }

    public String toString(){
        return "Index: " + index + " type: " + type;
    }
}
