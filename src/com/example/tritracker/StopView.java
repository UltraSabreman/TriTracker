package com.example.tritracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stop_view);
		
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
		//TODO is this running beofre activity is initialized propely?
		ListView view = (ListView) findViewById(R.id.UIBussList);
		ar = new BussArrayAdaptor(this, stop.BussLines);
		view.setAdapter(ar);
		
		for (Arrival a : rs.arrival)
			stop.BussLines.add(new Buss(a));
	
		ar.notifyDataSetChanged();
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
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
