package net.ednovak.nearby;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class NearPriLocationListener implements LocationListener {
	private final static String TAG = NearPriLocationListener.class.getName();

	// Start with invalid location so I know if there is a problem!
	public double lon = -190.0; // Normally -180, 180
	public double lat = -190.0; // Normally -90, 90
	private double bestAccuracySoFar = Double.MAX_VALUE;
	private long lastUpdateTS = Long.MAX_VALUE;
	private Context ctx = null;

	// Let's make this a singleton!
	private static NearPriLocationListener instance = null;
	private NearPriLocationListener(){
		// Block normal instantiation
	}
	// Provide singleton getInstance (that can be called static)
	public static NearPriLocationListener getInstance(Context newCtx){
		if(instance == null){
			instance = new NearPriLocationListener();
			instance.ctx = newCtx;
		}
		return instance;
	}


	
	@Override
	public void onLocationChanged(Location location) { // The callback
		if (location.hasAccuracy()){
			double newAcc = location.getAccuracy();

			if(newAcc < bestAccuracySoFar){ // If we have a better estimate
				updateCurrent(location);
				Log.d(TAG, "New best accuracy: " + bestAccuracySoFar);
				if(ctx != null){
					Toast.makeText(ctx, "Current Location Accuracy: " + bestAccuracySoFar + "m", Toast.LENGTH_SHORT).show();
				}
				bestAccuracySoFar = newAcc;
			}

			else if(NearPriLib.getTimeSince(lastUpdateTS) > 5000) { // If it's been more than five seconds
				updateCurrent(location);
			}

			else if(lon == -190.0 || lat == -190.0) { // If we literally have no idea yet
				updateCurrent(location);
			}
		}
	}
	
	
	private void updateCurrent(Location loc){
		// For the 'still listening function (maybe not used)
		lon = loc.getLongitude();
		lat = loc.getLatitude();

		lastUpdateTS = System.currentTimeMillis();

		Log.d(TAG, "Location updated.  Lon: " + lon + "  lat: "+ lat);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extra){ Log.d(TAG, "status changed"); }
	
	@Override
	public void onProviderEnabled(String provider) {
		Log.d(TAG, "providerEnabled");
	}
	
	@Override
	public void onProviderDisabled(String provider){
		Log.d(TAG, "providerDisabled");
	}


	public void plugFake(Context context){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if ( prefs.getBoolean("fake_locations", false) ){
			Double fake_lat = Double.valueOf(prefs.getString("fake_lat", "37.2708"));
			Double fake_lon = Double.valueOf(prefs.getString("fake_lon", "-76.7113"));
			lat = fake_lat;
			lon = fake_lon;
		}
	}
}
