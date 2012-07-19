package net.ednovak.nearby;

import java.math.BigInteger;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class paillierTest extends Activity{
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        
	}
	
	public void encryptionTest(View view){
		EditText et1 = (EditText)findViewById(R.id.bit_strength); 
		EditText et2 = (EditText)findViewById(R.id.certainty);
		EditText et3 = (EditText)findViewById(R.id.message);
		
		int bitStrength = Integer.valueOf(et1.getText().toString());
		int certainty = Integer.valueOf(et2.getText().toString());
		BigInteger message = new BigInteger(et3.getText().toString());
		
		TextView tv = (TextView)findViewById(R.id.output);
		tv.setText("Working...\n");
		
		long start = System.currentTimeMillis();
		
		tv.append("Generating " + et1.getText().toString() + " bit key...\n");
		
		Paillier paillier = new Paillier(bitStrength, certainty);
		
		tv.append("Encrypting '" + message.toString() + "'...\n");
		BigInteger enc = paillier.Encryption(message);
		
		tv.append("Encrypted: " + enc.toString(16), 0, 30);
		tv.append("...\n");
		
		tv.append("Decrypting...\n");
		BigInteger clear = paillier.Decryption(enc);
		tv.append("Clear: " + clear + "\n");
		
		long end = System.currentTimeMillis();
		long totalTime = end - start;
		tv.append("Test Done, total time: " + totalTime + "ms\n");	
	}
}


