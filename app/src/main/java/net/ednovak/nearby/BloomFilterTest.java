package net.ednovak.nearby;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class BloomFilterTest extends Activity {
    private final static String TAG = BloomFilterTest.class.getName();

    private final BloomFilter<Integer> bf = new BloomFilter<Integer>(.01, 30);
    public Context ctx;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bloom_filter_test);
        ctx = getApplicationContext();
    }


    public void addItem(View v){
        EditText et = (EditText)findViewById(R.id.numberToAdd);
        int newNum = Integer.valueOf(et.getText().toString());

        if(bf.contains(newNum)){
            Toast.makeText(ctx, newNum + " is already present!", Toast.LENGTH_LONG).show();
        }
        else{
            bf.add(newNum);
            Toast.makeText(ctx, "Adding " + newNum, Toast.LENGTH_SHORT).show();
        }
    }
}
