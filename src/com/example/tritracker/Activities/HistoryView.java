package com.example.tritracker.Activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.NavUtils;
import android.view.Menu;
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

public class HistoryView extends Activity implements UndoBarController.UndoListener{
	private UndoBarController mUndoBarController;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stop_list);
		Util.parents.push(getClass());

		getActionBar().setDisplayHomeAsUpEnabled(true);

		((TextView) findViewById(R.id.NoMembers)).setText("You have nothing in your history");
		
		mUndoBarController = new UndoBarController(findViewById(R.id.undobar), this);
		
		Util.subscribeToEdit(getApplicationContext(), this, R.id.UIStopIDBox);
		initList();
		onActivityChange();
	}
	
	private void onActivityChange() {
		if (GlobalData.History == null || GlobalData.History.size() == 0)
			((TextView) findViewById(R.id.NoMembers)).setVisibility(View.VISIBLE);
		else
			((TextView) findViewById(R.id.NoMembers)).setVisibility(View.INVISIBLE);
		
		GlobalData.Orientation = getResources().getConfiguration().orientation;
		GlobalData.histAdaptor.notifyDataSetChanged();
		Util.dumpData(getApplicationContext());
		
		findViewById(R.id.mainView).invalidate();
	}

	private void initList() {
		ListView view = (ListView) findViewById(R.id.UIStopList);
		GlobalData.histAdaptor = new StopArrayAdaptor(this, GlobalData.History, false);
		view.setAdapter(GlobalData.histAdaptor);
		GlobalData.histAdaptor.notifyDataSetChanged();

		final Activity testAct = (Activity)this;
		view.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				Stop temp = GlobalData.histAdaptor.getItem(position);
				if (temp != null) {
					GlobalData.CurrentStop = temp;
					new JSONRequestManger(getApplicationContext(), testAct, temp.StopID).start();
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
							Stop stop = GlobalData.histAdaptor.getItem(position);
							if (stop != null) {
								GlobalData.HUndos.add(stop);
								GlobalData.histAdaptor.remove(stop);
							
								mUndoBarController.showUndoBar(false, 
										"Removed " + GlobalData.HUndos.size() + " Stop" + (GlobalData.HUndos.size() > 1 ? "s" : ""),null);
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
		getMenuInflater().inflate(R.menu.history_view, menu);
		return true;
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
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		mUndoBarController.hideUndoBar(false);
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent parentActivityIntent = new Intent(this, Util.parents.pop());
			parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_NEW_TASK);
			NavUtils.navigateUpTo(this, parentActivityIntent);
			return true;
		case R.id.action_clear:
			for (Stop s : GlobalData.History)
				GlobalData.HUndos.add(s);
			
			GlobalData.histAdaptor.clear();
			
			mUndoBarController.showUndoBar(false, 
					"Removed " + GlobalData.HUndos.size() + " Stop" + (GlobalData.HUndos.size() > 1 ? "s" : ""),null);
			
			onActivityChange();
			return true;
		/*case R.id.action_search:
			Util.showToast("Not in yet", Toast.LENGTH_SHORT);
			return true;*/
		case R.id.action_sort:
			Util.buildSortDialog((Activity) this, 1);
			onActivityChange();
			return true;
		case R.id.action_settings:
			startActivity(new Intent(this, SettingsView.class));
			return true;

		}
		return super.onOptionsItemSelected(item);
	}
	
    @Override
    public void onUndo(Parcelable token, boolean fail) {
    	if (!fail) {
	        for (Stop s: GlobalData.HUndos) {
	        	if (!Util.histHasStop(s))
	        		GlobalData.histAdaptor.add(s);
	        }
	        GlobalData.HUndos.clear();
	        onActivityChange();
    	} else
    		GlobalData.HUndos.clear();
    }
}
