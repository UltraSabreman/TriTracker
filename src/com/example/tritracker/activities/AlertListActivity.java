package com.example.tritracker.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.example.tritracker.Alert;
import com.example.tritracker.R;
import com.example.tritracker.Util;
import com.example.tritracker.arrayadaptors.AlertListArrayAdaptor;

import java.util.ArrayList;

public class AlertListActivity extends Activity {
	AlertListArrayAdaptor ar;

	private MainService theService;
	private ArrayList<Alert> alerts = new ArrayList<Alert>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.alerts);
		Util.parents.push(getClass());

		getActionBar().setDisplayHomeAsUpEnabled(true);

		theService = MainService.getService();

		Bundle extras = getIntent().getExtras();
		alerts = theService.getStopAlerts(theService.getStop(extras.getInt("stop")));

		ar = new AlertListArrayAdaptor(getApplicationContext(), alerts);

		((ListView) findViewById(R.id.AlertList)).setAdapter(ar);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.alerts_actionbar, menu);
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
