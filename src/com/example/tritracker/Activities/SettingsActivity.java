package com.example.tritracker.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NavUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.example.tritracker.R;
import com.example.tritracker.Util;
import com.example.tritracker.activities.MainService.LocalBinder;

public class SettingsActivity extends Activity {

	private MainService theService;
	private boolean bound;
	
	@Override 
	public void onStart() {
		super.onStart();
		Intent intent = new Intent(this, MainService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
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
            

    		EditText np = (EditText) findViewById(R.id.Delay);
    		np.setText(String.valueOf(theService.getDelay()));

    		np.setOnEditorActionListener(new OnEditorActionListener() {
    			public boolean onEditorAction(TextView v, int actionId,
    					KeyEvent event) {
    				if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
    						|| (actionId == EditorInfo.IME_ACTION_DONE)) {
    					EditText edit = (EditText) findViewById(R.id.Delay);

    					theService.setDelay(Integer.parseInt(edit.getText().toString()));
    				}
    				return false;
    			}
    		});
    		
    		np = (EditText) findViewById(R.id.Radius);
    		np.setText(String.valueOf(Math.round(theService.getMapRadius() / 0.3408)));

    		np.setOnEditorActionListener(new OnEditorActionListener() {
    			public boolean onEditorAction(TextView v, int actionId,
    					KeyEvent event) {
    				if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
    						|| (actionId == EditorInfo.IME_ACTION_DONE)) {
    					EditText edit = (EditText) findViewById(R.id.Radius);

    					theService.setMapRadius(Double.parseDouble(edit.getText().toString()) * 0.3408);
    				}
    				return false;
    			}
    		});
    		 
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setTheme(android.R.style.Theme_Holo_);
		setContentView(R.layout.settings_layout);

		Util.parents.push(getClass());

		getActionBar().setDisplayHomeAsUpEnabled(true);

	}

	public void helpRefresh(View view) {
		Util.messageDiag(
				this,
				null,
				"Auto-refresh delay",
				"The delay (in seconds) at witch the app will refresh all stops. The higher the number, the less data-hungry it will be. Set to 0 to dissable.");
	}
	
	public void helpRadius(View view) {
		Util.messageDiag(
				this,
				null,
				"Map Search Radius",
				"When searching for nearby stops, the map will use this value to determine how far to look. 2048 feet is equal to a half a mile.");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings_action_bar, menu);
		return true;
	}

	private void goBack() {
		Intent parentActivityIntent = new Intent(this, Util.parents.pop());
		parentActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		NavUtils.navigateUpTo(this, parentActivityIntent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			goBack();

			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
