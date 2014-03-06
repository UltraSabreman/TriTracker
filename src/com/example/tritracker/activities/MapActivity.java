package com.example.tritracker.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.example.tritracker.R;
import com.example.tritracker.Timer;
import com.example.tritracker.Util;
import com.example.tritracker.json.AllRoutesJSONResult;
import com.example.tritracker.map.Map;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MapActivity extends Activity  {
	MainService theService = null;
	static Map theMap = null;

	String oldRoutes = null;
	boolean [] oldPicks = null;
	LatLng oldPos = null;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);

		Util.parents.push(getClass());

		getActionBar().setDisplayHomeAsUpEnabled(true);

		theService = MainService.getService();
		MapFragment frag = (MapFragment) getFragmentManager().findFragmentById(R.id.map);

		theMap = new Map(frag, getApplicationContext(), this);

		final Bundle extras = getIntent().getExtras();

		final LatLng targetPos = (extras != null ? new LatLng(extras.getDouble("lat"), extras.getDouble("lng")) : null);
		final int stopId = (extras != null ? extras.getInt("stopid") : -1);
		final String route = (extras != null ? extras.getString("route") : null);
		final int block = (extras != null ? extras.getInt("block") : -1);

		if (theService.getMapSave()) {
			oldRoutes = theService.getMapFilter();
			if (oldPicks == null)
				oldPicks = new boolean[] {false, false, false};
			oldPicks = theService.getMapSettings();
		}

		Util.createSpinner(this);
		final Timer delay = new Timer(0.1);
			delay.addCallBack("", new Timer.onUpdate() {
				@Override
				public void run() {
                    if (!theMap.isConnected() || theService.updatingMapRoutes) return;
					delay.stopTimer();
					theMap.setSearchLayerEnabled(true);
					theMap.setRouteLayerEnabled(true);
					theMap.setTrackingLayerEnabled(true);

					if (oldPicks != null) {
						if (!oldPicks[0])
							theMap.clearTrackingLayer();
						else
							theMap.TrackingLayerDraw(oldRoutes);

						if (!oldPicks[1])
							theMap.clearRouteLayer();
						else
							theMap.RouteLayerDraw(oldRoutes);
					}

					if (stopId != -1)
						theMap.showStop(theService.getStop(stopId), true);
					else {
						if (oldPicks != null && !oldPicks[2])
							theMap.clearSearchLayer();
						else
							theMap.showStop(null, false);
					}


					if (route != null) {
						theMap.RouteLayerDraw(route);
						theMap.TrackingLayerDraw(route);
						oldPicks = new boolean[]{true, true, true};
						oldRoutes = String.valueOf(route);
					}

					if (block != -1 && route != null)
						theMap.TrackingLayerTransition(theService.getStop(stopId), Integer.valueOf(route), block, true);

					Util.hideSpinner();
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
	public void onDestroy() {
		super.onDestroy();

		if (theService.getMapSave()) {
			theService.setMapFilter(oldRoutes);
			if (oldPicks != null)
				theService.setMapSettings(oldPicks);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				goBack();

				return true;
			case R.id.what_to_show:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);

				final CharSequence[] items = {"Live Buss Tracking", "Buss Routes", "Buss Search"};
				final boolean [] selected = {false, false, true};
				if (oldPicks != null) {
					selected[0] = oldPicks[0];
					selected[1] = oldPicks[1];
					selected[2] = oldPicks[2];
				}

				builder.setMultiChoiceItems(items, selected, new DialogInterface.OnMultiChoiceClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i, boolean b) {
						selected[i] = b;
					}
				});

				builder.setTitle("Enable Drawing Layers");
				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						oldPicks = selected;

						if (!selected[0])
							theMap.clearTrackingLayer();
						if (!selected[1])
							theMap.clearRouteLayer();
						if (!selected[2])
							theMap.clearSearchLayer();
						else
							theMap.showStop(null, false);

						if (selected[0] || selected[1])
							showRouteSelector(selected);
					}
				});

				builder.setNegativeButton("Cancel",	new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
					}
				});
				builder.create().show();
				return true;
			case R.id.action_settings:
				startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
				return true;
		}
		return super.onOptionsItemSelected(item);
	}


	void showRouteSelector(final boolean[] layers) {
		//TODO make this have a custom theme later.
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		final ArrayList<AllRoutesJSONResult.ResultSet.Route> test = theService.getRoutes();

		final CharSequence[] items = new CharSequence[test.size()];
		final boolean [] selected = new boolean[test.size()];

		List<String> t = null;
		if (oldRoutes != null)
			t = Arrays.asList(oldRoutes.split(","));

		for (int i = 0; i < test.size(); i++) {
			AllRoutesJSONResult.ResultSet.Route temp = test.get(i);
			items[i] = temp.desc;
			if (oldRoutes != null && t.contains(String.valueOf(temp.route)))
				selected[i] = true;
			else
				selected[i] = false;
		}

		builder.setMultiChoiceItems(items, selected,  new DialogInterface.OnMultiChoiceClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i, boolean b) {
				selected[i] = b;
			}
		});

		builder.setTitle("Select Routes");
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				StringBuilder str = new StringBuilder();
				for (int i = 0; i < selected.length; i++) {
					if (selected[i])
						str.append(test.get(i).route).append(",");
				}

				String toDraw = str.toString().replaceFirst(",$","");

				theMap.clearTrackingLayer();
				theMap.clearRouteLayer();

				oldRoutes = toDraw;

				if (toDraw.isEmpty()) return;

				if (layers[0])
					theMap.TrackingLayerDraw(toDraw);

				if (layers[1])
					theMap.RouteLayerDraw(toDraw);
			}
		});

		builder.setNeutralButton("Clear All", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int dfdf) {
				for (int i = 0; i < selected.length; i++)
					selected[i] = false;

				oldRoutes = "";

				theMap.clearTrackingLayer();
				theMap.clearRouteLayer();
			}
		});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			}
		});
		builder.create().show();

	}

}
