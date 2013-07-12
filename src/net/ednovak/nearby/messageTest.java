package net.ednovak.nearby;

import java.util.Random;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class messageTest extends Activity{
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message_test);	
	}

	public void genMessage(View view){
		EditText lengthEditText = (EditText)findViewById(R.id.length);
		int length = Integer.valueOf(lengthEditText.getText().toString());
		
		Random rng = new Random();
		String characters = "0123456789abcdef";
		char[] message = new char[length];
		for (int i = 0; i < message.length; i++){
			message[i] = characters.charAt(rng.nextInt(characters.length()));
		}
		
		EditText messageEditText = (EditText)findViewById(R.id.message);
		messageEditText.setText(new String(message));
	}
	
	public void sendMessage(View view){
		EditText messageEditText = (EditText)findViewById(R.id.message);
		String message = messageEditText.getText().toString();
		
		EditText recEditText = (EditText)findViewById(R.id.rec);
		String rec = recEditText.getText().toString();
		
		/*
		*EditText chunkSizeEditText = (EditText)findViewById(R.id.chunk);
		*int chunk = Integer.valueOf(chunkSizeEditText.getText().toString());
		*shareSingleton share = shareSingleton.getInstance();
		*share.chunk = chunk;
		*/
		
		protocol p = new protocol();
		shareSingleton share = shareSingleton.getInstance();
		share.messageTestStart = System.currentTimeMillis();
		p.sendFBMessage(rec, message, 10, "5", view.getContext());
		
	}
}
