package net.ednovak.nearby;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.LogRecord;

public class MainActivity extends Activity {
    private final static String TAG = MainActivity.class.getName();

	private LocationManager lManager;
    private Context ctx;
    protected NPSocket s;
    private NPHandler h;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ctx = getApplicationContext();
        h = new NPHandler(ctx);
        

        // Location listener stuff
        lManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        NPLocationListener myListener = NPLocationListener.getInstance(ctx);
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
                Protocol p = Protocol.getInstance(ctx, h);
                p.resetState();
                p.resetTree();
                try {
                    InetAddress addr = NPSocket.getAddressFromHost(NPSocket.SERVER_HOST);
                    int port = NPSocket.SERVER_PORT;
                    s = new NPSocket(addr, port);
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

        NPLocationListener ll = NPLocationListener.getInstance(ctx);
        String tmp = "lon: " + ll.getLocationCopy().getLongitude() + "  lat: " + ll.getLocationCopy().getLatitude();
        Toast.makeText(ctx, tmp, Toast.LENGTH_SHORT).show();
        //Log.d(TAG, tmp);


        // Start profiling step
        h.start = System.currentTimeMillis();

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
                    Protocol p = Protocol.getInstance(ctx, h);
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
