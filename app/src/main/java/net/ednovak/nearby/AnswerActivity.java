package net.ednovak.nearby;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class AnswerActivity extends Activity{
	
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait);
        
        // Get stuff from intent
        Intent intent = getIntent();
        String text = intent.getStringExtra("answer");
        boolean answer = intent.getBooleanExtra("found", false); // Default is we didn't find him!
        
        TextView tv = (TextView)findViewById(R.id.text_view);
        tv.setText(text);
        
        ImageView iv = (ImageView) findViewById(R.id.waiting);
        if ( answer ){
        	iv.setImageResource(R.drawable.happy);
        }
    	else {
    		iv.setImageResource(R.drawable.sad);
    	}
       

    } // End of onCreate

}
