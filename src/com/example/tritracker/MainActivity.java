package com.example.tritracker;

import java.io.File;

import com.example.tritracker.json.JsonRequest;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;


public class MainActivity extends Activity {
	FavStopArrayAdaptor favAdaptor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Util.parents.push(getClass());

		File t = new File(getString(R.string.data_path));
		t.delete();

		Util.initToast(getApplicationContext());
		Util.readData(getApplicationContext());
		Util.subscribeToEdit(getApplicationContext(), this, R.id.UIStopIDBox);
		initList();
	}

	@Override
	public void onResume() {
		favAdaptor.notifyDataSetChanged();
		super.onResume();
	}

	private void initList() {
		ListView view = (ListView) findViewById(R.id.UIStopList);

		favAdaptor = new FavStopArrayAdaptor(this, GlobalData.Favorites);
		view.setAdapter(favAdaptor);

		view.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				Stop temp = GlobalData.Favorites.get(position);
				if (temp != null) {
					GlobalData.CurrentStop = temp;
					new JsonRequest(getApplicationContext())
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
							favAdaptor.remove(favAdaptor.getItem(position));
							Toast.makeText(getApplicationContext(),
									"Removed Stop",
									android.R.integer.config_shortAnimTime)
									.show();
						}
						favAdaptor.notifyDataSetChanged();
					}
				});
		view.setOnTouchListener(touchListener);
		// Setting this scroll listener is required to ensure that during
		// ListView scrolling,
		// we don't look for swipes.
		view.setOnScrollListener(touchListener.makeScrollListener());
	}

	@Override
	public void onDestroy() {
		Util.dumpData(getApplicationContext());
		super.onDestroy();
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
		case R.id.action_order:
			Util.showToast("Not in yet", Toast.LENGTH_SHORT);		
		case R.id.action_search:
			Util.showToast("Not in yet", Toast.LENGTH_SHORT);
			return true;
		case R.id.action_settings:
			Util.showToast("Not in yet", Toast.LENGTH_SHORT);
			return true;
		case R.id.action_history:
			startActivity(new Intent(this, HistoryView.class));
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
