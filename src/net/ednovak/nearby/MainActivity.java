package net.ednovak.nearby;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.telephony.SmsManager;

public class MainActivity extends Activity {
	
	public final static String EXTRA_MESSAGE = "net.ednovak.nearby.MESSAGE";
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    */
    
    public void query(View view)
    {
        Intent intent = new Intent(this, displayMessageAct.class);
        EditText editText = (EditText) findViewById(R.id.other_user);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }
    
    public void sendSMS(View view){
    	SmsManager sms = SmsManager.getDefault();
    	EditText editText = (EditText) findViewById(R.id.other_user);
    	String message = editText.getText().toString();
    	sms.sendTextMessage("5556", null, message, null, null);
    }
    
    
}
