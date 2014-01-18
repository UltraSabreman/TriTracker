package com.example.tritracker.activities;

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
import com.example.tritracker.Util;
import com.example.tritracker.activities.MainService.LocalBinder;

public class MainActivity extends FragmentActivity implements
		ActionBar.OnNavigationListener {

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * current dropdown position.
	 */
	private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
	
	private MainService theService;
	private boolean bound;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);
		
		Util.initToast(getApplicationContext());
		bound = false;
		//service = 
		/*((TextView) findViewById(R.id.NoMembers))
				.setText("You have nothing in your favorites");*/

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
	}

	@Override 
	public void onStart() {
		super.onStart();
		Intent intent = new Intent(this, MainService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        bound = true;
	}
	
    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (bound) {
            unbindService(mConnection);
            bound = false;
        }
    }
    
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LocalBinder binder = (LocalBinder) service;
            theService = binder.getService();
            bound = true;
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
			getActionBar().setSelectedNavigationItem(
					savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// Serialize the current dropdown position.
		outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar()
				.getSelectedNavigationIndex());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.test, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		//mUndoBarController.hideUndoBar(false);
		switch (item.getItemId()) {
		case R.id.action_sort:
			//Util.buildSortDialog(getActivity(), 0);
			//startActivity(new Intent(this, TestActivity.class));

			return true;
		case R.id.action_settings:
			//startActivity(new Intent(getActivity().getApplicationContext(), SettingsActivity.class));
			return true;
		case R.id.action_history:
			//startActivity(new Intent(getActivity().getApplicationContext(), HistoryActivity.class));
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		// When the given dropdown item is selected, show its contents in the
		// container view.
		if (position == 0) {
			Fragment frag = new StopListFragment(true);
			getSupportFragmentManager().beginTransaction().replace(R.id.container, frag).commit();
		} else {
			Fragment frag = new StopListFragment(false);
			getSupportFragmentManager().beginTransaction().replace(R.id.container, frag).commit();
		}
		
		return true;
	}
}