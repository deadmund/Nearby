package net.ednovak.nearby;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

public class lListener implements LocationListener {
	
	public double lon = -190.0;
	public double lat = -190.0;
	
	public lListener(){
		// blank constructor yay!
	}
	
	@Override
	public void onLocationChanged(Location location) { // The callback
		Log.d("location", "The location I got: " + location);
		if (location.hasAccuracy()){
			if (location.getAccuracy() < 1.0){
				Log.d("location", "This location is good enough");
				// do it all really goes here
				
				updateCurrent(location);
			}
		}
		else {
			updateCurrent(location);
		}
	}
	
	
	private void updateCurrent(Location loc){
		// For the 'still listening function (maybe not used)
		lon = loc.getLongitude();
		lat = loc.getLatitude();
		
		// To make it available globally later
		shareSingleton share = shareSingleton.getInstance();
		share.lon = loc.getLongitude();
		share.lat = loc.getLatitude();
		
	}
	
	public boolean listening(){
		if ( lat == -190.0 || lon == -190.0 ){
			return true;
		}
		else{
			return false;
		}
		
	}
	
	@Override
	public void onStatusChanged(String provider, int status, Bundle extra){
		Log.d("location", "status changed");
	}
	
	@Override
	public void onProviderEnabled(String provider) {
		Log.d("location", "providerEnabled");
	}
	
	@Override
	public void onProviderDisabled(String provider){
		Log.d("location", "providerDisabled");
	}


}
