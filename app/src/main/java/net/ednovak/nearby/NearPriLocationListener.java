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
	private NPLocation bestLoc = new NPLocation();
	private Context ctx = null;
	private SharedPreferences sharedPrefs;

	// Let's make this a singleton!
	private static NearPriLocationListener instance = null;

	private NearPriLocationListener(Context newCtx){
		// Block normal instantiation
		bestLoc.setLongitude(-190.0);
		bestLoc.setLatitude(-190.0);
		bestLoc.setAccuracy(Float.MAX_VALUE);

        ctx = newCtx;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	}

	// Provide singleton getInstance (that can be called static)
	public static NearPriLocationListener getInstance(Context newCtx){
		if(instance == null){
			instance = new NearPriLocationListener(newCtx);
		}
		return instance;
	}


	
	@Override
	public void onLocationChanged(Location newLocation) { // The callback
		if (newLocation.hasAccuracy()){

			if(newLocation.getAccuracy() < bestLoc.getAccuracy()){ // If we have a better estimate
				updateCurrent(newLocation); // Now the bestLoc is the new location
				Log.d(TAG, "New best accuracy: " + bestLoc.getAccuracy());
				if(ctx != null){
					Toast.makeText(ctx, "Current Location Accuracy: " + bestLoc.getAccuracy() + "m", Toast.LENGTH_SHORT).show();
				}
			}

			else if(NearPriLib.getTimeSince(bestLoc.getTime()) > 5000) { // If it's been more than five seconds
				updateCurrent(newLocation);
			}

			// Update with something if we have no idea yet
			else if(bestLoc.getLatitude() == -190.0 || bestLoc.getLongitude() == -190.0) {
				updateCurrent(newLocation);
			}
		}
	}
	
	
	private void updateCurrent(Location loc){
		// For the 'still listening function (maybe not used)
		NPLocation tmp = new NPLocation(loc);
		bestLoc = tmp;

		Log.d(TAG, "Location updated.  Lon: " + bestLoc.getLongitude() + "  lat: "+ bestLoc.getLatitude());
	}

	public NPLocation getLocationCopy(){
		if(fakeAllowed()){
			Log.d(TAG, "Sending fake location!");
			return getFake();
		}
		else {
			NPLocation locationCopy = new NPLocation(bestLoc);
			return locationCopy;
		}
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


	public NPLocation getFake(){
		if(fakeAllowed()){
			double lat = Double.valueOf(sharedPrefs.getString("fake_lat", "37.2704431"));
			double lon = Double.valueOf(sharedPrefs.getString("fake_lon", "-76.7120411"));

			NPLocation newLoc = new NPLocation();
			newLoc.setLatitude(lat);
			newLoc.setLongitude(lon);

			return newLoc;
		}
		throw new IllegalStateException("Fake locations not allowed in app preferences!");
	}

	private boolean fakeAllowed(){
		boolean allowed = sharedPrefs.getBoolean("fake_locations", false);
		return allowed;
	}

}
