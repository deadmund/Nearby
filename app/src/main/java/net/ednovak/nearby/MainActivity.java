package net.ednovak.nearby;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import net.yishanhe.mobilesc.ot.BaseOTR;
import net.yishanhe.mobilesc.ot.BaseOTS;
import net.yishanhe.mobilesc.ot.Util;
import net.yishanhe.mobilesc.rsaOT.BasePrimeOTR;

import org.spongycastle.jce.ECNamedCurveTable;
import org.spongycastle.jce.spec.ECParameterSpec;
import org.spongycastle.math.ec.ECPoint;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class MainActivity extends Activity {
    private final static String TAG = MainActivity.class.getName();

	private LocationManager lManager;
    private Context ctx;
    protected NearPriSocket s;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ctx = getApplicationContext();
        

        // Location listener stuff
        lManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        NearPriLocationListener myListener = NearPriLocationListener.getInstance(ctx);
        // Get location updates from both GPS and NETWORK_PROVIDER (which is WiFi and cell signal)
        lManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, Integer.MAX_VALUE, myListener);
        lManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, Integer.MAX_VALUE, myListener);


        // Connect to server as Bob!
        connect();

        testCode();
    }
    
    
    // Creates the menu (from pressing menu button on main screen)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        return true;
    }
    
    
    // When the settngs button is selected
    @Override
    public boolean onOptionsItemSelected(MenuItem item){  	
    	
    	Log.d("main", "Selected: " + item.toString());
    	switch ( item.getItemId() ){
    		case R.id.settings:
    			startActivity(new Intent(this, Preferences.class));
    			return true;

            case R.id.reconnect:
                if(s != null) {
                    s.closeSocket();
                }
                s = null;
                connect();
                return true;

    		
    		default:
    			return super.onOptionsItemSelected(item);
    	}
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    }
    
    
    // When the activity ends
    @Override
    public void onDestroy(){
    	super.onDestroy();
    }


    public void connect(){
        // tmp server address and ip address
        Thread t = new Thread(new Runnable(){
            @Override
            public void run(){
                Protocol p = Protocol.getInstance(ctx);
                p.reset();
                try {
                    InetAddress addr = NearPriSocket.getAddressFromHost(NearPriSocket.SERVER_HOST);
                    int port = NearPriSocket.SERVER_PORT;
                    s = new NearPriSocket(addr, port);
                    Log.d(TAG, "Connected, yay!");

                    Thread t2 = new Thread(new ServerHandler());
                    t2.start();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        });
        t.start();
    }
    
    
    // Query Button
    public void query(View view) {
        // Get Location

        NearPriLocationListener ll = NearPriLocationListener.getInstance(ctx);
        String tmp = "lon: " + ll.getLocationCopy().getLongitude() + "  lat: " + ll.getLocationCopy().getLatitude();
        Toast.makeText(ctx, tmp, Toast.LENGTH_SHORT).show();
        //Log.d(TAG, tmp);

        // Start a query with Bob!  I'm not alice
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String IP = prefs.getString("bob_ip", "0.0.0.0");
        String msg = "a:" + IP;
        if(s != null) {
            s.writeSocket(msg.getBytes());
        } else{
            Toast.makeText(ctx, "Please reconnect to the server", Toast.LENGTH_SHORT).show();
        }
    }




    private class ServerHandler implements Runnable{

        @Override
        public void run(){
            while(true) {
                try {
                    byte[] newData = s.readSocket();

                    if (newData == null) {
                        break;
                    }
                    Protocol p = Protocol.getInstance(ctx);
                    byte[] ans = p.handleMsg(newData, ctx);
                    if (ans == null) {
                        break;
                    }
                    s.writeSocket(ans);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    break;
                }
            }
            if(s != null){
                s.closeSocket();
            }
            s = null;
        }
    }

    private void testCode() {
    }
}
