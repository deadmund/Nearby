package net.ednovak.nearby;

import android.util.Log;

import java.util.BitSet;
import java.util.Random;

/**
 * Created by ejnovak on 10/4/15.
 */
public class OTSelection{
    private final static String TAG = OTSelection.class.getName();

    private boolean[] sel;
    private int flipCount = 0;
    private int leadingZeros;

    int[] subSet;
    int subSetCur = 0;

    public OTSelection(int size){
        leadingZeros = 0;
        if(size % 8 != 0){
            leadingZeros = (size / 8) + 1;
        }


        sel = new boolean[size];
        subSet = new int[size];
    }

    public void flipToOne(int index){
        if(sel[index] == true){
            throw new IllegalArgumentException("Bit at " + index + " is already 1!");
        }
        sel[index] = true;
        subSet[subSetCur] = index;

        flipCount++;
    }

    public int get(int index){
        if(sel[index]){
            return 1;
        }
        else{
            return 0;
        }
    }


    public int flipCount(){
        return flipCount;
    }

    public int size(){
        return sel.length;
    }

    /**
     *
     * @return a deep copy of the backing bytes array
     */
    public byte[] getBytesCopy() {
        byte[] toReturn = new byte[sel.length / 8];
        for (int entry = 0; entry < toReturn.length; entry++) {
            for (int bit = 0; bit < 8; bit++) {
                if (sel[entry * 8 + bit]) {
                    toReturn[entry] |= (128 >> bit);
                }
            }
        }
        return toReturn;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < sel.length; i++){
            if(sel[i]){
                sb.append('1');
            } else {
                sb.append('0');
            }
        }
        return sb.toString();
    }

    public int[] toIntArray(){
        int[] ans = new int[sel.length];
        for(int i = 0; i < sel.length; i++){
            if(sel[i]){
                ans[i] = 1;
            } else {
                ans[i] = 0;
            }
        }
        return ans;
    }

    public int getLeadingZeros(){
        return leadingZeros;
    }

    public void flipHalf(){
        Random r = new Random();
        while(flipCount() < sel.length/2){
            try {
                flipToOne(r.nextInt(sel.length));
            } catch (IllegalArgumentException e){ };
        }
    }
}
