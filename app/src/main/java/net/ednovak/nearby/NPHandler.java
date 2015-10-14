package net.ednovak.nearby;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by ejnovak on 10/13/15.
 */
public class NPHandler extends Handler {
    private final static String TAG = NPHandler.class.getName();

    public final static int MSG_BOBS_LOCATION = 0;
    public long start;
    private Context ctx;

    public NPHandler(Context newCtx){
        ctx = newCtx;
    }

    @Override
    public void handleMessage(Message msg){
        Log.d(TAG, "New Message of Type: " + msg.what);

        switch(msg.what){
            case MSG_BOBS_LOCATION:
                Bundle b = msg.getData();
                String location = b.getString("location");
                Log.d(TAG, "location: " + location);

                long dur = NPLib.getTimeSince(start);
                Log.d(TAG, "Complete query took: " + dur + "ms");

                String[] parts = location.split(",");
                String lat = parts[0].split(":")[1];
                String lon = parts[1].split(":")[1];

                Intent i = new Intent(ctx, ViewLocation.class);
                i.putExtra("lat", lat);
                i.putExtra("lon", lon);
                i.putExtra("name", "Bob");
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(i);
        }

    }

}
