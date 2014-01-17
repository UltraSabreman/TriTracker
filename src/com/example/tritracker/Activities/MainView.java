package com.example.tritracker.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.example.tritracker.GlobalData;
import com.example.tritracker.R;
import com.example.tritracker.Stop;
import com.example.tritracker.Util;
import com.example.tritracker.ArrayAdaptors.StopArrayAdaptor;
import com.example.tritracker.NotMyCode.SwipeDismissListViewTouchListener;
import com.example.tritracker.NotMyCode.UndoBarController;
import com.example.tritracker.json.JSONRequestManger;

public class MainView extends Activity implements
		UndoBarController.UndoListener {
	private UndoBarController mUndoBarController;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stop_list);
		Util.parents.push(getClass());

		setTitle("Favorites");

	 //Write a service class that runs everthing and manages the app (stays alive and keeps track of shit)
	// write one main view that uses the dropdown navigation wiht your gutted mainview and history view taking
		//up the slots.
		//Keep the stopview alive after app exit (through service?) so clicking notification takes you there?
		// - Can also run notifications through service, make it init the stop view when clicked?
		
		//somehow make sure that the context is properly intilized.
		/*try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//wtf race condition*/
		
		Util.initToast(getApplicationContext());
		Util.readData(getApplicationContext());
		Util.subscribeToEdit(getApplicationContext(), this, R.id.UIStopIDBox);

		((TextView) findViewById(R.id.NoMembers))
				.setText("You have nothing in your favorites");

		mUndoBarController = new UndoBarController(findViewById(R.id.undobar),
				this);

		initList();
		Util.updateAllStops(getApplicationContext(), this);
		Util.restartTimer(getApplicationContext(), this);

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
			((TextView) findViewById(R.id.NoMembers))
					.setVisibility(View.VISIBLE);
		else
			((TextView) findViewById(R.id.NoMembers))
					.setVisibility(View.INVISIBLE);

		GlobalData.Orientation = getResources().getConfiguration().orientation;
		GlobalData.favAdaptor.notifyDataSetChanged();
		Util.dumpData(getApplicationContext());
		// findViewById(R.id.mainView).invalidate();
	}

	private void initList() {
		ListView view = (ListView) findViewById(R.id.UIStopList);
		GlobalData.favAdaptor = new StopArrayAdaptor(this,
				GlobalData.Favorites, true);
		view.setAdapter(GlobalData.favAdaptor);
		final Activity testAct = (Activity) this;
		view.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				Stop temp = GlobalData.favAdaptor.getItem(position);
				if (temp != null) {
					GlobalData.CurrentStop = temp;
					new JSONRequestManger(getApplicationContext(), testAct,
							temp.StopID).start();
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
							Stop stop = GlobalData.favAdaptor.getItem(position);
							if (stop != null) {
								GlobalData.FUndos.add(stop);
								GlobalData.favAdaptor.remove(stop);

								mUndoBarController.showUndoBar(
										false,
										"Removed "
												+ GlobalData.FUndos.size()
												+ " Stop"
												+ (GlobalData.FUndos.size() > 1 ? "s"
														: ""), null);
							}
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
		mUndoBarController.hideUndoBar(false);
		switch (item.getItemId()) {
		case R.id.action_sort:
			Util.buildSortDialog((Activity) this, 0);
			//startActivity(new Intent(this, TestActivity.class));
			
			onActivityChange();
			return true;
			/*
			 * case R.id.action_search: Util.showToast("Not in yet",
			 * Toast.LENGTH_SHORT); return true;
			 */
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsView.class));
			return true;
		case R.id.action_history:
			startActivity(new Intent(this, HistoryView.class));
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onUndo(Parcelable token, boolean fail) {
		if (!fail) {
			for (Stop s : GlobalData.FUndos) {
				if (!Util.favHasStop(s))
					GlobalData.favAdaptor.add(s);
			}
			GlobalData.FUndos.clear();
			onActivityChange();
		} else
			GlobalData.FUndos.clear();
	}

}
