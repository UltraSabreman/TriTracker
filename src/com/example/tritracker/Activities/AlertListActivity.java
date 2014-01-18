package com.example.tritracker.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.example.tritracker.R;
import com.example.tritracker.Stop;
import com.example.tritracker.Util;
import com.example.tritracker.arrayadaptors.AlertArrayAdaptor;

public class AlertListActivity extends Activity {
	AlertArrayAdaptor ar;
	
	private Stop curStop;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alert_list);
		Util.parents.push(getClass());

		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		Bundle extras = getIntent().getExtras();
		curStop = extras.getParcelable("stop");

		ar = new AlertArrayAdaptor(getApplicationContext(),	curStop.Alerts);

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
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
