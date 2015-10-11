package net.ednovak.nearby;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.BitSet;

/**
 * Created by ejnovak on 9/17/15.
 */
public class NearPriLib {
    private final static String TAG = NearPriLib.class.getName();

    public static long getTimeSince(long ts){
        long ans = System.currentTimeMillis() - ts;
        return ans;
    }

    public static String prettyArray(boolean[] input, char delim){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < input.length; i++){
            sb.append(input[i]);
            sb.append(delim);
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }

    public static String prettyArray(byte[][][] arr, char delim, char bigDelim, char massiveDelim){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < arr.length; i++){
            for(int j = 0; j < arr[i].length; j++){
                for(int k = 0; k < arr[i][j].length; k++){
                    sb.append(arr[i][j][k]);
                    sb.append(delim);
                }
                sb.append(bigDelim);
            }
            sb.append(massiveDelim);
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }

    public static String prettyArray(byte[][] arr, char delim, char bigDelim){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < arr.length; i++){
            for(int j = 0; j < arr[i].length; j++){
                sb.append(arr[i][j]);
                sb.append(delim);
            }
            sb.append(bigDelim);
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }

    public static String prettyArray(byte[] input, char delim){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i< input.length; i++){
            sb.append(input[i]);
            sb.append(delim);
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }


    public static String prettyArray(Object[] input, char delim){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < input.length; i++){
            sb.append(input[i].toString() + delim);
        }
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }

    public static String prettyArray(int[] input, char delim){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < input.length; i++){
            sb.append(input[i]);
            sb.append(delim);
        }
        sb = sb.deleteCharAt(sb.length()-1); // Remove the last delim
        return sb.toString();
    }

    public static String prettyArray(int[][] input, char delim, char bigDelim){
        // responseData[i][0] = NearPriLib.boolToInt(aliceBF[0].getBit(indexQuery[i]));
        // responseData[i][1] = NearPriLib.boolToInt(r.nextBoolean());
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < input.length; i++){
            sb.append(input[i][0]);
            sb.append(delim);
            sb.append(input[i][1]);
            sb.append(bigDelim);
        }

        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }

    public static int boolToInt(boolean in){
        if(in){
            return 1;
        }
        return 0;

    }

    public static byte boolToByte(boolean in){
        return (byte)boolToInt(in);
    }

    public static int[] bfSpotsToIntArray(ArrayList<BFSpot> input){
        int[] ans = new int[input.size()];
        for(int i = 0; i < input.size(); i++){
            ans[i] = input.get(i).index;
        }
        return ans;
    }

    public static void dump(String label, Object[] input, String delim){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < input.length; i++){
            sb.append(input[i] + delim);
        }
        sb.deleteCharAt(sb.length()-1);
        Log.d(TAG, label + sb.toString());
    }

    public static void dump(String label, int[] input, String delim){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < input.length; i++){
            sb.append(input[i] + delim);
        }
        sb.deleteCharAt(sb.length()-1);
        Log.d(TAG, label + sb.toString());
    }

    public static void dump(String label, ArrayList<? extends Object> input, String delim){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < input.size(); i++){
            sb.append(input.get(i).toString() + delim);
        }
        sb.deleteCharAt(sb.length()-1);
        Log.d(TAG, label + sb.toString());
    }


    public static String section(byte[] input, int s, int e){
        if (s < 0){ s = 0; }
        if (e > input.length-1) {e = input.length -1;}
        StringBuilder sb = new StringBuilder();
        sb.append("...");
        for(int i = s; i < e; i++){
            sb.append(input[i]);
            sb.append(",");
        }
        sb.deleteCharAt(sb.length()-1);
        sb.append("...");
        return sb.toString();
    }


}
