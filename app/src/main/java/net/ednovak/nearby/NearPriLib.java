package net.ednovak.nearby;

/**
 * Created by ejnovak on 9/17/15.
 */
public class NearPriLib {

    public static long getTimeSince(long ts){
        long ans = System.currentTimeMillis() - ts;
        return ans;
    }
}
