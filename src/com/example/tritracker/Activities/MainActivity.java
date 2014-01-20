package com.example.tritracker.activities;

import java.util.ArrayList;

import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import com.example.tritracker.R;
import com.example.tritracker.Sorter;
import com.example.tritracker.Stop;
import com.example.tritracker.Timer;
import com.example.tritracker.Util;
import com.example.tritracker.Util.ListType;
import com.example.tritracker.activities.MainService.LocalBinder;

public class MainActivity extends FragmentActivity implements ActionBar.OnNavigationListener {

	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
	private static boolean started = false;
	
	private StopListFragment favFrag;
	private StopListFragment histFrag;
	
	private MainService theService;
	private boolean bound;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Util.parents.push(getClass());
		
		Util.initToast(getApplicationContext());

		// Set up the action bar to show a dropdown list.
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		// Set up the dropdown list navigation in the action bar.
		actionBar.setListNavigationCallbacks(
		// Specify a SpinnerAdapter to populate the dropdown list.
				new ArrayAdapter<String>(actionBar.getThemedContext(),android.R.layout.simple_list_item_1, android.R.id.text1, 
							new String[] {
								"Favorites",
								"History", }), this);
		if (!started) {
			startService(new Intent(this, MainService.class));
			started = true;
		}
	}

	@Override 
	public void onStart() {
		super.onStart();
        bindService(new Intent(this, MainService.class), mConnection, Context.BIND_AUTO_CREATE);
	}
	
    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (bound)
            unbindService(mConnection);
    }
    
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            theService = binder.getService();
            bound = true;
            
            theService.sub("Main", new Timer.onUpdate() {
            	public void run() {
            	}
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// Restore the previously serialized current dropdown position.
		if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
			getActionBar().setSelectedNavigationItem(savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current dropdown position.
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar().getSelectedNavigationIndex());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_action_bar, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		//mUndoBarController.hideUndoBar(false);
		switch (item.getItemId()) {
		case R.id.action_sort:
			final int selction = getActionBar().getSelectedNavigationIndex();
			final ArrayList<Stop> tempStops = selction == 0 ? theService.getFavorties() : theService.getHistory();
			new Sorter<Stop>(Stop.class, theService).sortUI(this, selction == 0 ? ListType.Favorites : ListType.History,	tempStops,
					new Timer.onUpdate() {
						public void run() {
							if (selction == 0)
								favFrag.update(tempStops);
							else
								histFrag.update(tempStops);
							theService.doUpdate(false);
						}
					});
			return true;
		case R.id.action_settings:
			startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
			return true;
		case R.id.action_map:
			startActivity(new Intent(getApplicationContext(), MapActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		// When the given dropdown item is selected, show its contents in the
		// container view.
		while(theService == null)
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		
		if (position == 0) {
			favFrag = new StopListFragment(theService, true);
			getSupportFragmentManager().beginTransaction().replace(R.id.container, (Fragment)favFrag).commit();
		} else {
			histFrag = new StopListFragment(theService, false);
			getSupportFragmentManager().beginTransaction().replace(R.id.container, (Fragment)histFrag).commit();
		}
		
		return true;
	}
}
