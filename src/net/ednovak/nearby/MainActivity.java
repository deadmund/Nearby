package net.ednovak.nearby;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	public final static String EXTRA_MESSAGE = "net.ednovak.nearby.MESSAGE";
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);    
        
        SeekBar sb = (SeekBar)findViewById(R.id.seekbar);
        sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
        	
        	public void onStopTrackingTouch(SeekBar arg0){}
        	
        	public void onStartTrackingTouch(SeekBar arg0){}
    
        	public void onProgressChanged(SeekBar sb, int progress, boolean isUser){
        		TextView tv = (TextView)findViewById(R.id.seekbar_text);
        		int value = (int)((progress - 10) / 29.9);
        		tv.setText(Integer.toString(value)+ "m");
        	}
        });
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    */
    
    public void query(View view) {
        Intent intent = new Intent(this, displayMessageAct.class);
        EditText editText = (EditText) findViewById(R.id.other_user);
        String message = editText.getText().toString();
        if (message.length() != 0 && message != null){
        	intent.putExtra(EXTRA_MESSAGE, message);
        	startActivity(intent);
        }
        else {
        	Context context = getApplicationContext();
        	Toast.makeText(context, "You need to provide a phone number", Toast.LENGTH_SHORT).show();
        }
    }
    
    /*
    public void sendSMS(View view){
    	SmsManager sms = SmsManager.getDefault();
    	EditText editText = (EditText) findViewById(R.id.other_user);
    	String message = editText.getText().toString();
    	sms.sendTextMessage("5556", null, message, null, null);
    }
    */
    
    
}
