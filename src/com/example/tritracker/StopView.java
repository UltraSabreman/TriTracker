package com.example.tritracker;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.AndroidCharacter;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.example.tritracker.json.Arrival;
import com.example.tritracker.json.ResultSet;
import com.example.tritracker.json.results;
import com.google.gson.Gson;

public class StopView extends Activity {
	private Stop stop;
	private BussArrayAdaptor ar;
	private ArrayList<Buss> busses = new ArrayList<Buss>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stop_view);
		Util.parents.push(getClass());
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
				
		Intent intent = getIntent();
		String json = intent.getStringExtra("JSON Stop");
		
      	Gson gson = new Gson();
    	results test = gson.fromJson(json, results.class);
    	
    	ResultSet rs = test.resultSet;
    	
    	stop = new Stop(rs.location[0]);
    	
    	setTitle(stop.Name);
		
    	initList(rs);    	
	}
	
	void initList(ResultSet rs) {
		ListView view = (ListView) findViewById(R.id.UIBussList);
		ar = new BussArrayAdaptor(this, busses);
		view.setAdapter(ar);
		
		if (rs.arrival != null)
		{
			for (Arrival a : rs.arrival)
				busses.add(new Buss(a));
	
			ar.notifyDataSetChanged();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.stop_view, menu);
		return true;
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
        	Intent parentActivityIntent = new Intent(this, Util.parents.pop());
        	parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        	//startActivity(parentActivityIntent);
            NavUtils.navigateUpTo(this, parentActivityIntent);
            
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
