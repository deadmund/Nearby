package net.ednovak.nearby;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

public class sendMessageFB extends Activity {
	Facebook facebook = new Facebook("396962547019321");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fb);

        facebook.authorize(this, new DialogListener() {
            @Override
            public void onComplete(Bundle values) {
            	stuff();
            }

            @Override
            public void onFacebookError(FacebookError error) {}

            @Override
            public void onError(DialogError e) {}

            @Override
            public void onCancel() {}
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        facebook.authorizeCallback(requestCode, resultCode, data);
    }
    
    public void stuff(){
    	Log.d("Facebook", "Testing if this callback works dawg");
    	Bundle params = new Bundle();
    	params.putString("to", String.valueOf(5));    	
    	facebook.dialog(this, "send", params, null);
    }
}
