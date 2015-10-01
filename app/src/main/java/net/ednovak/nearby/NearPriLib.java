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
}
