package com.example.tritracker.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.example.tritracker.map.Map;
import com.example.tritracker.R;
import com.example.tritracker.Timer;
import com.example.tritracker.Util;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.net.ConnectException;


public class MapActivity extends Activity  {
	MainService theService = null;
	Map test = null;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);

		Util.parents.push(getClass());

		getActionBar().setDisplayHomeAsUpEnabled(true);

		theService = MainService.getService();
		MapFragment frag = (MapFragment) getFragmentManager().findFragmentById(R.id.map);

		test = new Map(frag, getApplicationContext(), this);

		final Bundle extras = getIntent().getExtras();

		final LatLng targetPos = (extras != null ? new LatLng(extras.getDouble("lat"), extras.getDouble("lng")) : null);

		final Timer delay = new Timer(0.1);
			delay.addCallBack("", new Timer.onUpdate() {
				@Override
				public void run() {
					try {
						test.setSearchLayerEnabled(true);
						test.showStops(null);
						delay.stopTimer();
					} catch (ConnectException e) {}
				}
			});
		delay.restartTimer();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map_actionbar, menu);
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
			case R.id.action_settings:
				startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
				return true;
		}
		return super.onOptionsItemSelected(item);
	}




}
