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
		
		long keyStart = System.currentTimeMillis();
		Paillier paillier = new Paillier(bitStrength, certainty);
		long keyEnd = System.currentTimeMillis();
		
		tv.append("Encrypting '" + message.toString() + "'...\n");
		long encStart = System.currentTimeMillis();
		BigInteger enc = paillier.Encryption(message);
		long encEnd = System.currentTimeMillis();
		
		tv.append("Encrypted: " + enc.toString(16), 0, 30);
		tv.append("...\n");
		
		tv.append("Decrypting...\n");
		long decStart = System.currentTimeMillis();
		BigInteger clear = paillier.Decryption(enc);
		long decEnd = System.currentTimeMillis();
		tv.append("Clear: " + clear + "\n");
		
		long end = System.currentTimeMillis();
		long totalKey = keyEnd - keyStart;
		long totalEnc = encEnd - encStart;
		long totalDec = decEnd - decStart;
		long totalTime = end - start;
		
		tv.append("Test Done\n");
		tv.append("Time to generate key: " + totalKey + "ms\n");
		tv.append("Time to Encrypt: " + totalEnc + "ms\n");
		tv.append("Time to Decrypt: " + totalDec + "ms\n");
		tv.append("Total Time: " + totalTime + "ms\n");	
	}
}


