package net.ednovak.nearby;

import android.location.Location;
import android.util.Log;

/**
 * Created by ejnovak on 9/23/15.
 */
public class NPLocation extends Location{
    private final static String TAG = NPLocation.class.getName();

    public NPLocation(Location loc){
        super(loc);
    }

    public NPLocation(){
        super("");
    }

    public double getLongitudeRadians(){
        return this.getLongitude() * Math.PI / 180.0;
    }

    public double getLatitudeRadians(){
        return this.getLatitude() * Math.PI / 180.0;
    }

    public String toPrettyString(){
        String tmp = "lat: " + this.getLatitude() + ", lon: " + this.getLongitude();
        return tmp;
    }

    // Return distance from this point to another point (in km)
    // Uses Haversin formula to calculate distance between two points.  Use that to know which
    // 10m (distance bewteen south pole and lat line, etc)
    public double haversineDistanceTo(NPLocation other){

        double lat1R = this.getLatitudeRadians();
        double lat2R = other.getLatitudeRadians();

        double lon1R = this.getLongitudeRadians();
        double lon2R = other.getLongitudeRadians();

        /*
        NPLocation tmp1 = new NPLocation();
        tmp1.setLatitude(36.12);
        tmp1.setLongitude(-86.67);

        NPLocation tmp2 = new NPLocation();
        tmp2.setLatitude(33.94);
        tmp2.setLongitude(-118.40);

        // Should output:  2887.2599506071106 from this site:
        http://rosettacode.org/wiki/Haversine_formula#Java

        double lat1R = tmp1.getLatitudeRadians();
        double lat2R = tmp2.getLatitudeRadians();

        double lon1R = tmp1.getLongitudeRadians();
        double lon2R = tmp2.getLongitudeRadians();
        */


        double dLatR = lat2R - lat1R;
        double dLonR = lon2R - lon1R;

        double a = Math.pow(Math.sin(dLatR / 2.0), 2) + Math.pow( Math.sin(dLonR/2.0), 2) * Math.cos(lat1R) * Math.cos(lat2R) ;
        double c = 2 * Math.asin(Math.sqrt(a));
        double d = (6372800 * c);
        //Log.d(TAG, "Calculating Distance  a: " + a + "  c: " + c + "  d: " + d);

        return d;
    }


    public static NPLocation getIDLfromLat(double lat){
        // International date line
        NPLocation IDL = new NPLocation();
        IDL.setLongitude(180);
        IDL.setLatitude(lat);
        return IDL;
    }

    public static NPLocation getSouthPole(){
        // distance from south pole in meters
        NPLocation southPole = new NPLocation();
        southPole.setLongitude(0);
        southPole.setLatitude(-90);
        return southPole;
    }



}
