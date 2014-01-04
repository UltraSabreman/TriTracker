package com.example.tritracker.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.example.tritracker.GlobalData;
import com.example.tritracker.R;
import com.example.tritracker.Util;
import com.example.tritracker.ArrayAdaptors.AlertArrayAdaptor;

public class AlertListView extends Activity {
	AlertArrayAdaptor ar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alert_list);
		Util.parents.push(getClass());

		getActionBar().setDisplayHomeAsUpEnabled(true);

		
		ar = new AlertArrayAdaptor(getApplicationContext(), GlobalData.CurrentStop.Alerts);
	
		((ListView) findViewById(R.id.AlertList)).setAdapter(ar);		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_alert_list, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent parentActivityIntent = new Intent(this, Util.parents.pop());
			parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_NEW_TASK);
			NavUtils.navigateUpTo(this, parentActivityIntent);

			return true;
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsView.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
