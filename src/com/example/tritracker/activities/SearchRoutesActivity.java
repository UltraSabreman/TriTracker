package com.example.tritracker.activities;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.example.tritracker.R;
import com.example.tritracker.Timer;
import com.example.tritracker.Util;
import com.example.tritracker.arrayadaptors.RouteListArrayAdaptor;
import com.example.tritracker.json.AllRoutesJSONResult.ResultSet.Route;

public class SearchRoutesActivity extends Activity {
	private MainService theService;
	private ArrayList<Route> routes = new ArrayList<Route>();
	private RouteListArrayAdaptor ar;
	private Timer test;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_routes);
		Util.parents.push(getClass());

		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		theService = MainService.getService();
		
		if (!theService.isUpdating)
			init();
		else {
			Util.createSpinner(this);
			test = new Timer(0.5);
			test.addCallBack("check valid", new Timer.onUpdate() {
				@Override
				public void run() {
					if (!theService.isUpdating) {			
						init();
						Util.hideSpinner();
						test.stopTimer();
					}
				}
			});
			test.restartTimer();
		}
	}

	private void init() {
		routes = theService.getRoutes();
		
		if (routes != null) {
			ListView view = (ListView) findViewById(R.id.RouteList);
			ar = new RouteListArrayAdaptor(getApplicationContext(), routes);
			view.setAdapter(ar);
			ar.notifyDataSetChanged();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.search_routes, menu);
		return true;
	}
	

	private void goBack() {
		Intent parentActivityIntent = new Intent(this, Util.parents.pop());
		parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		NavUtils.navigateUpTo(this, parentActivityIntent);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			goBack();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
