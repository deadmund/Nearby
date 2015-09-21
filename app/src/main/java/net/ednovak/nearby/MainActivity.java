package net.ednovak.nearby;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;

public class MainActivity extends Activity {
    private final static String TAG = MainActivity.class.getName();

	private LocationManager lManager;
    private Context ctx;
    protected Socket s;
    protected OutputStream sockOut;
    protected InputStream sockIn;
    public static final int BYTE_COUNT = 64;

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
                closeSocket();
                connect();
                return true;

    			
    		case R.id.test_encryption:
    	    	startActivity(new Intent(this, paillierTest.class));
    	    	return true;
    	    	
    		case R.id.test_message: // Test sending messages on FB
    			if (xmppService.in){
    				startActivity(new Intent(this, messageTest.class));
    				return true;
    			}
    			else{
					Toast.makeText(this, "You must be logged into Facebook", Toast.LENGTH_SHORT).show();
					return false;
    			}

            case R.id.bf_test:
                Intent i = new Intent(this, BloomFilterTest.class);
                startActivity(i);
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

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                String SERVER_IP = "ec2-52-89-134-220.us-west-2.compute.amazonaws.com";
                int SERVER_PORT = 5555;

                try {
                    // Create Socket
                    InetAddress serverAddr = InetAddress.getByName(SERVER_IP);
                    s = new Socket(serverAddr, SERVER_PORT);

                    // Wire up streams for input and output in other functions / threads
                    sockOut = s.getOutputStream();
                    sockIn = s.getInputStream();

                    // Create thread to handle socket input
                    // Make sure to do this before sending the 'b' (below)
                    // so that we can pickup / read the response from the server!
                    Thread t = new Thread(new ServerHandler());
                    t.start();

                    writeSocket("b:");
                }
                catch(Exception e){
                    Log.d(TAG, "Something went wrong with the dumb socket!");
                    e.printStackTrace();
                    closeSocket();
                }
            }
        });
        t.start();
    }
    
    
    // Query Button
    public void query(View view) {
        // Get Location

        NearPriLocationListener ll = NearPriLocationListener.getInstance(ctx);
        String tmp = "lon: " + ll.lon + "  lat: " + ll.lat;
        Toast.makeText(ctx, tmp, Toast.LENGTH_SHORT).show();
        //Log.d(TAG, tmp);

        // Start a query with Bob!  I'm not alice
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String IP = prefs.getString("bob_ip", "0.0.0.0");
        String msg = "a:" + IP;
        writeSocket(msg);

    }


    private void closeSocket(){
        if(s != null) {
            while (!s.isClosed()) {
                try {
                    s.close();
                } catch (IOException e) {
                }
            }
        }
        sockOut = null;
        sockIn = null;
        Log.d(TAG, "Socket closed");
    }

    private void writeSocket(String msg){
        StringBuilder sb = new StringBuilder(msg);
        while(sb.length() < BYTE_COUNT){
            sb.append("@");
        }

        try {
            sockOut.write(sb.toString().getBytes());
            Log.d(TAG, "Sent " + sb.toString());
        } catch (IOException e){
            e.printStackTrace();
            closeSocket();
        } catch(NullPointerException e1){
            e1.printStackTrace();
            Toast.makeText(ctx, "Please reconnect to server", Toast.LENGTH_SHORT).show();
        }

    }

    private class ServerHandler implements Runnable{

        public ServerHandler(){

        }


        private int readLoop(byte[] buffer){
            int sumBytes = 0;
            while(sumBytes < BYTE_COUNT){
                int newAmount;
                try {
                    newAmount = sockIn.read(buffer, sumBytes, buffer.length - sumBytes);
                } catch (IOException e){
                    e.printStackTrace();
                    return -1;
                }
                sumBytes += newAmount;
                if(newAmount < 0){
                    return -1;
                }
                if(newAmount < BYTE_COUNT){
                    Log.d(TAG, "Only read " + newAmount + " bytes instead of " + BYTE_COUNT + "! Reading more.");
                }
            }
            return sumBytes;
        }

        @Override public void run(){
            while(true) {
                byte[] buffer = new byte[BYTE_COUNT];
                int total = readLoop(buffer);
                if(total == -1){
                    break;
                }


                String data = new String(buffer, Charset.forName("UTF-8"));
                data = data.replace("@","");
                Log.d(TAG, "Data Received: " + data);


                // Just a temporary test thingy
                if (data.equals("ack-a")) {
                    Log.d(TAG, "Sending \'Hello Bob!\' to other device");
                    writeSocket("Hello Bob!");
                }


                if (data.equals("ack-b")) {
                    Log.d(TAG, "Idling as Bob");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    writeSocket("b:2");
                }


                if (data.equals("Hello Bob!")) {
                    writeSocket("Hello Alice!");
                }
            }
            closeSocket();
        }
    }
}
