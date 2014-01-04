package com.example.tritracker.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tritracker.GlobalData;
import com.example.tritracker.R;
import com.example.tritracker.Stop;
import com.example.tritracker.Util;
import com.example.tritracker.ArrayAdaptors.StopArrayAdaptor;
import com.example.tritracker.NotMyCode.SwipeDismissListViewTouchListener;
import com.example.tritracker.json.ActiveJSONRequest;

public class MainView extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stop_list);
		Util.parents.push(getClass());

		Util.initToast(getApplicationContext());
		Util.readData(getApplicationContext());
		Util.subscribeToEdit(getApplicationContext(), this, R.id.UIStopIDBox);

		((TextView) findViewById(R.id.NoMembers)).setText("You have nothing in your favorites");
		
		
		initList();
		Util.updateAllStops(getApplicationContext());
		Util.restartTimer(getApplicationContext());
		
		onActivityChange();
	}

	@Override
	public void onResume() {
		onActivityChange();
		super.onResume();
	}
	
	@Override
	public void onRestart() {
		onActivityChange();
		super.onRestart();
	}
	
	@Override
	public void onDestroy() {
		onActivityChange();
		super.onDestroy();
	}

	private void onActivityChange() {
		if (GlobalData.Favorites == null || GlobalData.Favorites.size() == 0)
			((TextView) findViewById(R.id.NoMembers)).setVisibility(View.VISIBLE);
		else
			((TextView) findViewById(R.id.NoMembers)).setVisibility(View.INVISIBLE);
				
		GlobalData.Orientation = getResources().getConfiguration().orientation;
		GlobalData.favAdaptor.notifyDataSetChanged();
		Util.dumpData(getApplicationContext());
	}
	
	private void initList() {
		ListView view = (ListView) findViewById(R.id.UIStopList);
		GlobalData.favAdaptor = new StopArrayAdaptor(this, GlobalData.Favorites, true);
		view.setAdapter(GlobalData.favAdaptor);
		final Activity testAct = (Activity)this;

		view.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				Stop temp = GlobalData.Favorites.get(position);
				if (temp != null) {
					GlobalData.CurrentStop = temp;
					new ActiveJSONRequest(getApplicationContext(), testAct)
							.execute("http://developer.trimet.org/ws/V1/arrivals?locIDs="
									+ temp.StopID
									+ "&json=true&appID="
									+ getString(R.string.appid));
				}
			}
		});

		// Create a ListView-specific touch listener. ListViews are given
		// special treatment because
		// by default they handle touches for their list items... i.e. they're
		// in charge of drawing
		// the pressed state (the list selector), handling list item clicks,
		// etc.
		SwipeDismissListViewTouchListener touchListener = new SwipeDismissListViewTouchListener(
				view, new SwipeDismissListViewTouchListener.DismissCallbacks() {
					@Override
					public boolean canDismiss(int position) {
						return true;
					}

					@Override
					public void onDismiss(ListView listView,
							int[] reverseSortedPositions) {
						for (int position : reverseSortedPositions) {
							GlobalData.favAdaptor.remove(GlobalData.favAdaptor.getItem(position));
							Toast.makeText(getApplicationContext(),
									"Removed Stop",
									android.R.integer.config_shortAnimTime)
									.show();
						}
						onActivityChange();
					}
				});
		view.setOnTouchListener(touchListener);
		// Setting this scroll listener is required to ensure that during
		// ListView scrolling,
		// we don't look for swipes.
		view.setOnScrollListener(touchListener.makeScrollListener());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_action_bar, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_sort:
			Util.buildSortDialog((Activity) this, 0);
			onActivityChange();
			return true;
		case R.id.action_search:
			Util.showToast("Not in yet", Toast.LENGTH_SHORT);
			return true;
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsView.class));
			return true;
		case R.id.action_history:
			startActivity(new Intent(this, HistoryView.class));
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
