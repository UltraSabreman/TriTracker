package com.example.tritracker;

import com.example.tritracker.json.JsonRequest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class HistoryView extends Activity {
	StopArrayAdaptor histAdaptor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_history_view);
		Util.parents.push(getClass());

		getActionBar().setDisplayHomeAsUpEnabled(true);

		Util.subscribeToEdit(getApplicationContext(), this, R.id.UIStopIDBox);
		initList();
	}

	@Override
	public void onResume() {
		histAdaptor.notifyDataSetChanged();
		super.onResume();
	}

	private void initList() {
		ListView view = (ListView) findViewById(R.id.UIHistoryList);
		histAdaptor = new StopArrayAdaptor(this, GlobalData.History, false);
		view.setAdapter(histAdaptor);

		Intent intent = getIntent();
		int count = intent.getIntExtra("count", 0);
		for (int i = 0; i < count; i++) {
			GlobalData.History
					.add((Stop) intent.getParcelableExtra("Stop" + i));
		}

		histAdaptor.notifyDataSetChanged();

		final Activity testAct = (Activity)this;
		view.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				Stop temp = GlobalData.History.get(position);
				if (temp != null) {
					GlobalData.CurrentStop = temp;
					new JsonRequest(getApplicationContext(), testAct)
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
							histAdaptor.remove(histAdaptor.getItem(position));
							Toast.makeText(getApplicationContext(),
									"Removed Stop",
									android.R.integer.config_shortAnimTime)
									.show();
						}
						histAdaptor.notifyDataSetChanged();
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
		getMenuInflater().inflate(R.menu.history_view, menu);
		return true;
	}
	@Override
	public void onRestart() {
		GlobalData.Orientation = getResources().getConfiguration().orientation;
		histAdaptor.notifyDataSetChanged();
		super.onRestart();
	}
	
	@Override
	public void onDestroy() {
		GlobalData.Orientation = getResources().getConfiguration().orientation;
		histAdaptor.notifyDataSetChanged();
		super.onDestroy();
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent parentActivityIntent = new Intent(this, Util.parents.pop());
			parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_NEW_TASK);
			// startActivity(parentActivityIntent);
			NavUtils.navigateUpTo(this, parentActivityIntent);
			return true;
		case R.id.action_clear:
			GlobalData.History.clear();
			Util.showToast("History Cleared", Toast.LENGTH_SHORT);
			histAdaptor.notifyDataSetChanged();
			return true;
		case R.id.action_search:
			Util.showToast("Not in yet", Toast.LENGTH_SHORT);
			return true;
		case R.id.action_sort:
			Util.showToast("Not in yet", Toast.LENGTH_SHORT);
			return true;
		case R.id.action_settings:
			Util.showToast("Not in yet", Toast.LENGTH_SHORT);
			return true;

		}
		return super.onOptionsItemSelected(item);
	}
}
