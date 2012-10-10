package net.ednovak.nearby;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class processedQueries extends ListActivity {
	
	private static ArrayList<String> data = new ArrayList<String>();
	
	// Should be called when activity is newly opened
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_processed_queries);
		
		Log.d("processed", "Anything at all");
		
		Intent i = getIntent();
		
		String lat = i.getStringExtra("lat");
		String lon = i.getStringExtra("lon");
		String name = i.getStringExtra("name");
		
		
		ListView list = (ListView)findViewById(android.R.id.list);
		//data.add(name + ": " + lat + ", " + lon);
		data.add(name + ": " + lat + ", " + lon);
		ArrayAdapter<String> AA = new ArrayAdapter<String>(this, R.layout.smallfont, data);
		list.setAdapter(AA);
	}
}
